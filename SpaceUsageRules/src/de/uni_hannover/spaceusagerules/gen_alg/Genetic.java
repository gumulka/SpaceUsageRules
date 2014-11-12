package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

import de.uni_hannover.spaceusagerules.core.CoordinateInMa;
import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.KML;
import de.uni_hannover.spaceusagerules.io.OSM;

/** 
 * TODO die javadoc ins englische umschreiben.
 * Klasse zum durchlaufen eines Genetischen Algorithmus.
 */
public class Genetic extends Thread implements Comparable<Genetic>{

  	/** Die Anzahl der Populationen, welche den Algorithmus durchlaufen sollen */
	public static  int popsize = 200;
	/** Dei Anzahl der Runden, die ohne Optimierung durchlaufen werden, bevor der Algorithmus stoppt. */
	public static  int withoutOtimization = 300;
  	/** Die Anzahl der Populationen, welche unbearbeitet in die nächste generation übernommen werden sollen */
	public static  int copyBest = popsize*1/10;
  	/** Die Anzahl der Populationen, welche mutiert in die nächste Generation übernommen werden solllen. */
	public static  int mutate = popsize*4/10;
  	/** Die Anzahl der Populationen pro Generation, welche aus anderen zusammen gesetzt werden sollen */
	public static  int merge = popsize*3/10;
  	/** Die Menge der Poplationen, aus denen die Populationen zusammen gesetzt werden sollen. */
	public static  int mergeFrom = popsize/2;
	
  	/** Die Liste der Polygone, welche das richtige Ergebnis representieren. */
	private List<Geometry> truths;
  	/** die Liste der Coordinaten, von welchem aus die Lösungspolygone gesucht werden sollen */
	private List<CoordinateInMa> starting;
  	/** Eine Liste von Listen, welche die Möglichen Lösungen Respresentieren. */
	private List<Set<Way>> possebilities;
  	/** Die Liste der Populationen, welche aktuell bearbeitet werden. */ 
	private List<Population> pops, nextGen;
  	/** der Name der SpaceUsageRule, nach der Optimiert werden soll. */
	private String suche;
	
	/** Die Menge der möglichen Tags, welche einen Einfluss auf die Auswertung haben können. */
	private Collection<String> possible;
	/** wenn ein SigInt abgefangen wird, dann wird kill auf true gesetzt und beendet den Algorithmus vorzeitig. */
	public static boolean kill = false;
	
	public Genetic(String signlist, Set<String> IDs, Collection<String> possible) throws Exception {
		suche = signlist;
		this.possible = possible;
		truths = new ArrayList<Geometry>();
		starting = new ArrayList<CoordinateInMa>();
		possebilities = new ArrayList<Set<Way>>();
		pops = new ArrayList<Population>();
		nextGen = new ArrayList<Population>();

		for(int i = 0; i<popsize; i++) {
			pops.add(new Population(possible));
		}
		for(String s : IDs) {
				String filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%s.jpg",s);
				CoordinateInMa c = Image.readCoordinates(filename); 
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
		if(nextGen.size() != pops.size()) {
			System.err.println("Unterschiedliche Längen!!!!");
		}
		pops = nextGen;
		nextGen = new ArrayList<Population>();
	}
	
  	/**
     * die Methode zum durchlaufen des Genetischen algorithmus mit der Steuerung aus den statischen Variablen.
     */
	@SuppressWarnings("unused")
	public void run() {
		if(mutate + merge + copyBest > popsize) {
//			System.err.println("Population ist going to grow. Aborting.");
			return; 
		}
		int i = 0;
		int oldFitness = -1;
		int verbesserungen = -1;
		int letzteVerbesserung = 0;
		int letzteOptimierung = 0;
		int optimierungen = 0;
		int rules = 0;
		int fitness;
		while(true) {
			if(kill)
				break;
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
			if(i-letzteVerbesserung > withoutOtimization && i-letzteOptimierung > withoutOtimization)
				break;
			i++;
		}
//		System.out.println(i + suche + " -> " + pops.get(0));
//		System.out.println(verbesserungen + " Verbesserungen (" + letzteVerbesserung + ") und " + optimierungen + " Optimierungen (" + letzteOptimierung + ") seitdem");
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

	/**
	 * Vergleich um die Gentischen Algorithmen nach ihrer geschätzten Laufzeit zu sortieren.
	 */
	public int compareTo(Genetic o) {
		return o.truths.size() - this.truths.size();
	}
	
}