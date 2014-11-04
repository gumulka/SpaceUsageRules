package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Polyline;
import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.KML;
import de.uni_hannover.spaceusagerules.io.OSM;

/** 
 * Klasse zum durchlaufen eines Genetischen Algorithmus.
 */
public class Genetic extends Thread{

  	/** Die anzahl der Populationen, welche den Algorithmus durchlaufen sollen */
	private static final int popsize = 300;
  	/** die Anzahl der Runden, welche Maximal durchlaufen werden sollen, bevor der Algorithmus abgebrochen wird. */
	private static final int maxRounds = 50000;
  	/** Die Anzahl der Runden, welche Mindestens durchlaufen werden sollen, selbst wenn der Wert schon erreicht ist. */
	private static final int minRounds = 20000;
  	/** Die Anzahl der Populationen, welche unbearbeitet in die nächste generation übernommen werden sollen */
	private static final int copyBest = popsize*1/10;
  	/** Die Anzahnl der Populationen, welche mutiert in die nächste Generation übernommen werden solllen. */
	private static final int mutate = popsize*4/10;
  	/** Die Anzahl der Populationen pro Generation, welche aus anderen zusammen gesetzt werden sollen */
	private static final int merge = popsize*3/10;
  	/** Die Menge der Poplationen, aus denen die Populationen zusammen gesetzt werden sollen. */
	private static final int mergeFrom = popsize/2;
  	/** Die Minimale Fitness, welche erreicht werden soll um den Algorithmus zu beenden. */
	private static final int targetMinFitness = Population.maxFitness*95/100;
	
  	/** Die Liste der Polygone, welche das richtige Ergebnis representieren. */
	private List<Polyline> truths;
  	/** die Liste der Coordinaten, von welchem aus die Lösungspolygone gesucht werden sollen */
	private List<Coordinate> starting;
  	/** Eine Liste von Listen, welche die Möglichen Lösungen Respresentieren. */
	private List<Set<Way>> possebilities;
  	/** Die Liste der Populationen, welche aktuell bearbeitet werden. */ 
	private List<Population> pops, nextGen;
  	/** der Name der SpaceUsageRule, nach der Optimiert werden soll. */
	private String suche;
	
  
	public Genetic(String sign) throws Exception {
		suche = sign;
		truths = new ArrayList<Polyline>();
		starting = new ArrayList<Coordinate>();
		possebilities = new ArrayList<Set<Way>>();
		pops = new ArrayList<Population>();
		nextGen = new ArrayList<Population>();

		for(int i = 0; i<popsize; i++) {
			pops.add(new Population());
		}
		
		File f = new File("../SpaceUsageRulesVis/assets/Data.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] bla = line.split(",");
			if(bla[3].trim().equals(sign)) {
				int i = Integer.parseInt(bla[0]);
				String filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%04d.jpg",i);
				InputStream is = new FileInputStream(new File(filename));
				Coordinate c = Image.readCoordinates(is); 
				starting.add(c);
				filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%04d.truth.kml",i);
				truths.add(KML.loadKML(new File(filename)));
				possebilities.add(OSM.getObjectList(c));
			}
		}
		br.close();
	}
	
	private void calcFitness() {
		for(Population p : pops) {
			p.calcFitness(truths, possebilities, starting);
		}
	}
	
	/**
     * erzeugt die nächste generation, indem die alte sortiert und dann neue erzeugt werden.
     */
  	private void nextGen() throws IOException {
		Collections.sort(pops);
		for(int i = 0; i<copyBest; i++) {
			nextGen.add(pops.get(i));
		}
		Random r = new Random();
		for(int i = 0; i<merge; i++) {
			int a = r.nextInt(mergeFrom);
			int b = r.nextInt(mergeFrom);
			nextGen.add(pops.get(a).recombine(pops.get(b)));
		}
		for(int i = 0; i<mutate; i++) {
			Population p = new Population(pops.get(i));
			nextGen.add(p);
		}
		for(int i = 0; i<(popsize-merge-copyBest-mutate); i++) {
			nextGen.add(new Population());
		}
		pops = nextGen;
		nextGen = new ArrayList<Population>();
	}
	
  	/**
     * die Methode zum durchlaufen des Genetischen algorithmus mit der Steuerung aus den statischen finalen Variablen.
     */
	public void run() {
		for(int i = 0; i<maxRounds; i++) {
			calcFitness();
			try {
				nextGen();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(i>minRounds && pops.get(0).getFitness()>targetMinFitness)
				break;
		}
		if(pops.size()!=popsize)
			System.out.println(pops.size());
		System.out.println(suche + "(" + truths.size() + ") -> " + pops.get(0));
	//	System.out.println(new Date());
	}

  	/**
     * Gibt die beste Population zurück. 
     */
	public Population getBest() {
		return pops.get(0);
	}
	
  	/**
     * gibt den Namen der SpaceUsageRule zurück
     */
	public String getRule() {
		return suche;
    }
	
}