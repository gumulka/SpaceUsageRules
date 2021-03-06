package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.drew.imaging.ImageProcessingException;

import de.uni_hannover.spaceusagerules.core.ThreadScheduler;
import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.OSM;

/**
 * TODO die Javadoc schreiben.
 * Klasse, welche für jede SpaceUsageRule aus dem Testdatensatz einen genetischen Algorithmus erstellt und diesen durchlaufen lässt.
 */
public class Main extends Thread implements Comparable<Main>{

	public static final int MAXTHREADS = 4;
	public static int DISSMISSED = 1;
	public static final int CPUCORES = 4;
	public static final double AUSLASTUNG = 1.0 - (1.0/(CPUCORES-MAXTHREADS));
	
	
	private static List<String> possibla = null;
	private static Map<String,Set<String>> tags = null;
	private static int max = 0;
	private static boolean prepared = false;
	
	
	protected List<Genetic> allGens;
	private int diff = Integer.MAX_VALUE;
	private int fitness = 0;
	
	public static final String path = "../Testdatensatz/";
	
	
	public static void prepare() throws IOException, ImageProcessingException {
		OSM.useBuffer(true);
		
		Map<String,Integer> possibilities = new TreeMap<String,Integer>();
		tags = new TreeMap<String,Set<String>>();
		File f = new File( path + "Data.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String lastID = null;
		String tag = "";
		String line;
		line = br.readLine();
		if(max == 0) 
			max = Integer.parseInt(line);
		// für alle zusammenstellungen von Tags einen Genetischen algorithmus machen.
		for(int i = 0; i<max;i++) {
			line = br.readLine();
			if(line == null)
				break;
			String[] bla = line.split(",");
			if(bla[0].equals(lastID)) {
				tag += bla[3].trim() + ", ";
			}
			else {
				// fügt der Liste aller möglichen möglichkeiten die in der Umgebung dieses Tags vorkommenden hinzu.
				String filename = String.format(Locale.GERMAN,path + "%s.jpg",bla[0]);
				for(Way w : OSM.getObjectList(Image.readCoordinates(filename))) {
					for(Entry<String,String> e : w.getTags().entrySet()) {
						Integer value = possibilities.get(e.getKey());
						if(value == null) 
							possibilities.put(e.getKey(), 0);
						else
							possibilities.put(e.getKey(), ++value);
						value = possibilities.get(e.getKey() + " - " + e.getValue());
						if(value == null) 
							possibilities.put(e.getKey() + " - " + e.getValue(), 0);
						else
							possibilities.put(e.getKey() + " - " + e.getValue(), ++value);
					}
				}
				// das letzte Komma abschneiden.
				if(tag.contains(",")) {
					tag = tag.substring(0, tag.lastIndexOf(','));
				}
				if(tag.length()!=0) {
					Set<String> IDs = tags.get(tag);
					if(IDs == null) {
						IDs = new TreeSet<String>();
						tags.put(tag,IDs);
					}
					IDs.add(lastID);
				}
				tag = bla[3].trim() + ", ";
				lastID = bla[0];
			}
		}
		br.close();
		
		possibla = new LinkedList<String>();
//		int kicked = 0;
		for(Entry<String,Integer> e : possibilities.entrySet())
			if(e.getValue()>DISSMISSED)
				possibla.add(e.getKey());
//			else
//				kicked++;
//		System.out.println("rauschgeschmissen: " +kicked + "/" + possibilities.size() + " möglichen Regeln");
		
//		System.out.println(tags.size() + " verschiedene Tags.");
		prepared = true;
	}
	
	public void run() {
		if(!prepared)
			return;
		if(mu+me>9)
			return;
		allGens = new ArrayList<Genetic>();
		Population.GENERATOR = gen;
		for(Entry<String,Set<String>> e: tags.entrySet())
			try {
				allGens.add(new Genetic(e.getKey(),e.getValue(), possibla,p,w,p/10,p*mu/10,p*me/10,p/5));
			} catch (Exception e1) {
				e1.printStackTrace();
				System.err.println("Fehler beim erstellen der Genetics");
				return;
			}
		Collections.sort(allGens);
		
		long startTime = System.currentTimeMillis();		
		ThreadScheduler.schedule(allGens, MAXTHREADS);
		diff = (int) ((System.currentTimeMillis() - startTime)/1000);

		
		for(Genetic g : allGens) {
			fitness += g.getBest().getFitness();
		}

		
		for(Genetic g : allGens) {
			r1 += g.roundsFind;
		}
		for(Genetic g : allGens) {
			r2 += g.roundsPolygon;
		}
		
		
		if(fitness > 0 || diff>10)
			System.out.println(this);
	}
	
	int r1=0,r2=0;
	
	public void writeout() throws IOException {
		File f = new File(path + "Rules.gen." + max + ".txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		for(Genetic g : allGens) {
			bw.write("[ " + g.getRule());
			bw.write("] -> ");
			bw.write(g.getBest().toString());
			bw.newLine();
		}
		bw.close();
	}	
	
	public int getDiff()  {
		return diff;
	}
	
	public int getFitness() {
		return fitness;
	}
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws ImageProcessingException
	 */
	public static void main(String[] args) throws Exception,
			IOException {
		 OSM.useBuffer(true);
		 
		 /*
		 Main test = new Main(200,200,2,5);
		 test.run();
		 System.out.println(test);
		 test.writeout(); // */
		 
		 
		 int[] disses = {1,5,10,15}; // 1,5,10
		 int[] generators =  {4,16,64}; // 4,16,64
		 int[] popsizes = {10,30,60,100,200,500};
		 int[] withouts = {0,1,3,6,10,20,50,100,200};
		 int[] mutates =  {7,5,3,1}; 
		 int[] merges = {7,5,3,1};
		 for(int m : disses) {
			 Main.DISSMISSED = m;
			 Main.prepare();
			 List<Thread> gens = new LinkedList<Thread>();
			 for(int g: generators) 
			 for(int p : popsizes) {
				 for(int w: withouts) {
					 for(int mu : mutates) {
						 for(int me : merges) {
								while(true) {
									if(gens.size()<3) {
										Thread x = new Main(p,w,mu,me,g);
										x.start();
										gens.add(x);
										break;
									} else
										try {
											Thread.sleep(100);
										} catch (InterruptedException ex) {
										}
									for(Thread r : gens)
										if(!r.isAlive()) {
											gens.remove(r);
											break;
										}
								}
						 }
					 }
				 }
			 }
			 System.err.println(new Date());
			 System.err.println("Runde fertig!");
		 } 
	}
	
	private int p,w,mu,me,gen;
	public Main(int p, int w, int mu, int me,int gen) {
		this.p = p;
		this.w = w;
		this.mu = mu;
		this.me = me;
		this.gen = gen;
	}

	
	public String toString() {
		String ret = String.format("Fitness: %6d, Laufzeit: %d:%02d:%02d ",fitness, diff/3600, (diff/60)%60, diff%60);
		ret += String.format("Pop: %d, Wi: %d, Mu: %d, Me: %d, Gen: %d ",p,w,mu,me,gen);
		ret += String.format("RoundsFind: %d, RoundsPolygon: %d Dissmissed: %d",r1/allGens.size(),r2/allGens.size(), DISSMISSED);
		return ret;
	}
	
	public int compareTo(Main o) {
		int f = o.getFitness() - fitness;
		if(f==0)
			return this.getDiff() - o.getDiff();
		return f;
	}
}