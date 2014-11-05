package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.File;
import java.io.FileInputStream;
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
public class Genetic extends Thread implements Comparable<Genetic>{

  	/** Die anzahl der Populationen, welche den Algorithmus durchlaufen sollen */
	private static final int popsize = 300;
  	/** die Anzahl der Runden, welche Maximal durchlaufen werden sollen, bevor der Algorithmus abgebrochen wird. */
	private static final int maxRounds = 50000;
  	/** Die Anzahl der Runden, welche Mindestens durchlaufen werden sollen, selbst wenn der Wert schon erreicht ist. */
	private static final int minRounds = 1000;
  	/** Die Anzahl der Populationen, welche unbearbeitet in die nächste generation übernommen werden sollen */
	private static final int copyBest = popsize*1/10;
  	/** Die Anzahnl der Populationen, welche mutiert in die nächste Generation übernommen werden solllen. */
	private static final int mutate = popsize*4/10;
  	/** Die Anzahl der Populationen pro Generation, welche aus anderen zusammen gesetzt werden sollen */
	private static final int merge = popsize*3/10;
  	/** Die Menge der Poplationen, aus denen die Populationen zusammen gesetzt werden sollen. */
	private static final int mergeFrom = popsize/2;
  	/** Die Minimale Fitness, welche erreicht werden soll um den Algorithmus zu beenden. */
	private static final int targetMinFitness = Population.maxFitness*90/100;
	
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
	
	private Set<String> possible;
	
	public Genetic(String signlist, Set<String> IDs, Set<String> possible) throws Exception {
		suche = signlist;
		this.possible = possible;
		truths = new ArrayList<Polyline>();
		starting = new ArrayList<Coordinate>();
		possebilities = new ArrayList<Set<Way>>();
		pops = new ArrayList<Population>();
		nextGen = new ArrayList<Population>();

		for(int i = 0; i<popsize; i++) {
			pops.add(new Population(possible));
		}
		for(String s : IDs) {
				String filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%s.jpg",s);
				InputStream is = new FileInputStream(new File(filename));
				Coordinate c = Image.readCoordinates(is); 
				starting.add(c);
				filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%s.truth.kml",s);
				truths.add(KML.loadKML(new File(filename)));
				possebilities.add(OSM.getObjectList(c));
		}
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
			Population p = new Population(pops.get(i),possible);
			nextGen.add(p);
		}
		for(int i = 0; i<(popsize-merge-copyBest-mutate); i++) {
			nextGen.add(new Population(possible));
		}
		pops = nextGen;
		nextGen = new ArrayList<Population>();
	}
	
  	/**
     * die Methode zum durchlaufen des Genetischen algorithmus mit der Steuerung aus den statischen finalen Variablen.
     */
	public void run() {
		int i = 0;
		int oldFitness = -1;
		int verbesserungen = -1;
		int letzteVerbesserung = 0;
		int letzteOptimierung = 0;
		int optimierungen = 0;
		int rules = 0;
		int fitness;
		for(i = 0; i<maxRounds; i++) {
			calcFitness();
			try {
				nextGen();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fitness = pops.get(0).getFitness();
			if(oldFitness<fitness) {
				oldFitness = fitness;
				letzteVerbesserung = i;
				verbesserungen++;
				rules = pops.get(0).getNumberOfRules();
				optimierungen = 0;
			}
			else if(pops.get(0).getNumberOfRules()<rules){
				rules = pops.get(0).getNumberOfRules();
				optimierungen++;
				letzteOptimierung = i;
			}
			if(i>minRounds && pops.get(0).getFitness()>targetMinFitness)
				break;
		}
		if(pops.size()!=popsize)
			System.out.println(pops.size());
		System.out.println(i + suche + " -> " + pops.get(0));
		System.out.println(verbesserungen + " Verbesserungen (" + letzteVerbesserung + ") und " + optimierungen + " Optimierungen (" + letzteOptimierung + ") seitdem");
	}

  	/**
     * Gibt die beste Population zurück. 
     */
	public Population getBest() {
		return pops.get(0);
	}
	
  	/**
     * gibt die Namen der SpaceUsageRules zurück
     */
	public String getRule() {
		return suche;
    }

	@Override
	public int compareTo(Genetic o) {
		return o.truths.size() - this.truths.size();
	}
	
}