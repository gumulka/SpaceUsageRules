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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.algorithm.Rules;
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
	public int popsize = 200;
	/** Dei Anzahl der Runden, die ohne Optimierung durchlaufen werden, bevor der Algorithmus stoppt. */
	public int withoutOtimization = 300;
  	/** Die Anzahl der Populationen, welche unbearbeitet in die nächste generation übernommen werden sollen */
	public int copyBest = popsize*1/10;
  	/** Die Anzahl der Populationen, welche mutiert in die nächste Generation übernommen werden solllen. */
	public int mutate = popsize*4/10;
  	/** Die Anzahl der Populationen pro Generation, welche aus anderen zusammen gesetzt werden sollen */
	public int merge = popsize*3/10;
  	/** Die Menge der Poplationen, aus denen die Populationen zusammen gesetzt werden sollen. */
	public int mergeFrom = popsize/2;
	
  	/** Die Liste der Polygone, welche das richtige Ergebnis representieren. */
	private List<Geometry> truths;
  	/** die Liste der Coordinaten, von welchem aus die Lösungspolygone gesucht werden sollen */
	private List<Point> starting;
  	/** Eine Liste von Listen, welche die Möglichen Lösungen Respresentieren. */
	private List<Collection<Way>> possebilities;
  	/** Die Liste der Populationen, welche aktuell bearbeitet werden. */ 
	private List<Population> pops, nextGen;
  	/** der Name der SpaceUsageRule, nach der Optimiert werden soll. */
	private String suche;
	
	/** Die Menge der möglichen Tags, welche einen Einfluss auf die Auswertung haben können. */
	private Collection<String> possible;
	/** wenn ein SigInt abgefangen wird, dann wird kill auf true gesetzt und beendet den Algorithmus vorzeitig. */
	public static boolean kill = false;

	private static GeometryFactory gf = new GeometryFactory();
	
	public Genetic(String signlist, Set<String> IDs, Collection<String> possible, int popsize, int without, int copyBest, int mutate, int merge, int mergeFrom) throws Exception {
		this.popsize = popsize;
		this.withoutOtimization = without;
		this.copyBest = copyBest;
		this.mutate = mutate;
		this.merge = merge;
		this.mergeFrom = mergeFrom;
		
		suche = signlist;
		this.possible = possible;
		truths = new ArrayList<Geometry>();
		starting = new ArrayList<Point>();
		possebilities = new ArrayList<Collection<Way>>();
		pops = new ArrayList<Population>();
		nextGen = new ArrayList<Population>();

		for(int i = 0; i<popsize; i++) {
			pops.add(new Population(possible));
		}
		for(String s : IDs) {
				String filename = String.format(Locale.GERMAN,Main.path + "/%s.jpg",s);
				Point c = gf.createPoint(Image.readCoordinates(filename)); 
				starting.add(c);
				filename = String.format(Locale.GERMAN,Main.path + "/%s.truth.kml",s);
				truths.add(KML.loadKML(new File(filename)));
				possebilities.add(OSM.getObjectList(c.getCoordinate()));
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
  	private void nextGen1() throws IOException {
		Collections.sort(pops);
		for(int i = 0; i<copyBest; i++) {
			nextGen.add(pops.get(i));
		}
		Random r = new Random();
		for(int i = 0; i<merge; i++) {
			int a = r.nextInt(mergeFrom);
			int b = r.nextInt(mergeFrom);
			nextGen.add(Population.merge(pops.get(a),pops.get(b)));
		}
		for(int i = 0; i<mutate; i++) {
			nextGen.add(Population.mutate(pops.get(i),possible));
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
     * erzeugt die nächste generation, indem die alte sortiert und dann neue erzeugt werden.
     */
  	private void nextGen2() throws IOException {
		Collections.sort(pops);
		for(int i = 0; i<copyBest; i++) {
			nextGen.add(pops.get(i));
		}
		Random r = new Random();
		for(int i = 0; i<merge; i++) {
			int a = r.nextInt(mergeFrom);
			int b = r.nextInt(mergeFrom);
			nextGen.add(Population.merge2(pops.get(a),pops.get(b)));
		}
		for(int i = 0; i<mutate; i++) {
			Population p = Population.mutate2(pops.get(i),possible);
			nextGen.add(p);
		}
		for(int i = 0; i<(popsize-merge-copyBest-mutate); i++) {
			nextGen.add(Population.generate2(pops.get(i),possible));
		}
		if(nextGen.size() != pops.size()) {
			System.err.println("Unterschiedliche Längen!!!!");
		}
		pops = nextGen;
		nextGen = new ArrayList<Population>();
	}

	static long f1=0,f2=0,g1=0,g2=0;
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
		long current, old;
		while(!kill) {
			old = System.currentTimeMillis();
			calcFitness();
			current = System.currentTimeMillis();
			try {
				nextGen1();
			} catch (IOException e) {
				e.printStackTrace();
			}
			g1 += (System.currentTimeMillis()-current);
			f1 += (current - old);
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
//		System.out.println(suche + " -> " + pops.get(0));
//		System.out.println(verbesserungen + " Verbesserungen (" + letzteVerbesserung + ") und " + optimierungen + " Optimierungen (" + letzteOptimierung + ") seitdem");

		Population best = pops.get(0);
		pops.clear();
		for(int x = 0; x<popsize;x++) {
			pops.add(best);
		}
		
		i-=withoutOtimization;
		rules = Integer.MAX_VALUE;
		while(!kill) {
			old = System.currentTimeMillis();
			calcFitness();
			current = System.currentTimeMillis();
			try {
				nextGen2();
			} catch (IOException e) {
				e.printStackTrace();
			}
			g2 += (System.currentTimeMillis()-current);
			f2 += (current-old);
			fitness = pops.get(0).getFitness();
			if(oldFitness<fitness) {
				oldFitness = fitness;
				letzteVerbesserung = i;
				verbesserungen++;
				rules = pops.get(0).getNumberOfThresholds();
				optimierungen = 0;
			}
			else if(pops.get(0).getNumberOfThresholds()<rules){
				rules = pops.get(0).getNumberOfThresholds();
				optimierungen++;
				letzteOptimierung = i;
			}
			if(i-letzteVerbesserung > withoutOtimization && i-letzteOptimierung > withoutOtimization)
				break;
			i++;
		}
		long bla = (System.currentTimeMillis() - TryAndRun.startTime);
		System.out.println("---------");
		double part = 100.0/bla;
//		System.out.printf("%2.1f %2.1f %.1f %.1f\n",part*f1,part*g1,part*f2,part*g2);
		System.out.printf("%.1f %.1f %.1f\n",part*Population.cal, part*Population.inter, part*Population.over);
		System.out.printf("%.1f %.1f %.1f\n",part*Rules.calcD,part*Rules.areas,part*Rules.thres);
		System.out.printf("%.1f %.1f %.1f\n",part*Rules.dist,part*Rules.wei,part*Rules.tagAdd);
//		System.out.println(suche + " -> " + pops.get(0));
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
		// TODO wieder umdrehen.
		return this.truths.size() - o.truths.size();
	}
	
}