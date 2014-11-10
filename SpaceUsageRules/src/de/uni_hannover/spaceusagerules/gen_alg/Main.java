package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.drew.imaging.ImageProcessingException;

import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.OSM;

/**
 * @todo die Javadoc schreiben.
 * Klasse, welche für jede SpaceUsageRule aus dem Testdatensatz einen genetischen Algorithmus erstellt und diesen durchlaufen lässt.
 */
public class Main extends Thread implements Comparable<Main>{

	public static final int MAXTHREADS = 2;
	
	private static List<String> possibla = null;
	private static Map<String,Set<String>> tags = null;
	private static int max = 0;
	private static boolean prepared = false;
	
	
	private List<Genetic> allGens;
	private int diff = Integer.MAX_VALUE;
	private int fitness = 0;
	
	
	public static void prepare() throws IOException, ImageProcessingException {
		OSM.useBuffer(true);
		
		Map<String,Integer> possibilities = new TreeMap<String,Integer>();
		tags = new TreeMap<String,Set<String>>();
		File f = new File("../SpaceUsageRulesVis/assets/Data.txt");
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
				String filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%s.jpg",bla[0]);
				InputStream is = new FileInputStream(new File(filename));
				for(Way w : OSM.getObjectList(Image.readCoordinates(is))) {
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
		int kicked = 0;
		for(Entry<String,Integer> e : possibilities.entrySet())
			if(e.getValue()>1)
				possibla.add(e.getKey());
			else
				kicked++;
		System.out.println("rauschgeschmissen: " +kicked + "/" + possibilities.size() + " möglichen Regeln");
		
		System.out.println(tags.size() + " verschiedene Tags.");
		prepared = true;
	}
	
	public void run() {
		if(!prepared)
			return;
		long startTime = System.currentTimeMillis();
		allGens = new ArrayList<Genetic>();
		for(Entry<String,Set<String>> e: tags.entrySet())
			try {
				allGens.add(new Genetic(e.getKey(),e.getValue(), possibla));
			} catch (Exception e1) {
				System.err.println("Fehler beim erstellen der Genetics");
				return;
			}
		Collections.sort(allGens);
		List<Genetic> gens = new LinkedList<Genetic>();
		for(Genetic g : allGens) {
			while(true) {
				if(gens.size()<MAXTHREADS) {
					g.start();
					gens.add(g);
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
		for(Genetic g : gens) {
			try {
				g.join();
			} catch (InterruptedException e1) {
			}
		}

		for(Genetic g : allGens) {
			fitness += g.getBest().getFitness();
		}
		
		diff = (int) ((System.currentTimeMillis() - startTime)/1000);
		if(fitness > 0 || diff>10)
			System.out.println(this);
	}
	
	public void writeout() throws IOException {
		File f = new File("../SpaceUsageRulesVis/assets/Rules.gen." + max + ".txt");
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
		 Runtime.getRuntime().addShutdownHook(new Thread()
	        {
	            @Override
	            public void run()
	            {
	            	Genetic.kill = true;
	            	System.out.println("Shutdown hook ran!");
	            }
	        });
		 OSM.useBuffer(true);
		 
		 /*
		 Main test = new Main(200,200,2,5);
		 test.run();
		 System.out.println(test);
		 test.writeout(); // */
		 
		 
		 
		 int[] maxis = {90};
		 int[] popsizes = {100,200,350,500};
		 int[] withouts = { 500};
		 int[] mutates =  {1, 3, 5};
		 int[] merges = {1, 3, 5, 6, 7};
		 for(int m : maxis) {
			 List<Main> all = new LinkedList<Main>();
			 Main.max = m;
			 Main.prepare();
			 for(int p : popsizes) {
				 for(int w: withouts) {
					 for(int mu : mutates) {
						 for(int me : merges) {
							 Main test = new Main(p,w,mu,me);
							 test.run();
							 all.add(test);
						 }
					 }
				 }
			 }
			 Collections.sort(all);
			 all.get(0).writeout();
		 } // */ 
	}
	
	private int p,w,mu,me;
	public Main(int p, int w, int mu, int me) {
		this.p = p;
		this.w = w;
		this.mu = mu;
		this.me = me;
		 Genetic.popsize = p;
		 Genetic.withoutOtimization = w;
		 Genetic.mutate = p*mu/10;
		 Genetic.merge = p*me/10;
		 Genetic.copyBest = p/10;
		 Genetic.mergeFrom = p/2;
	}

	
	public String toString() {
		String ret = String.format("Fitness: %6d, Laufzeit: %d:%02d:%02d ",fitness, diff/3600, (diff/60)%60, diff%60);
		ret += String.format("Pop: %d, Wi: %d, Mu: %d, Me: %d",p,w,mu,me);
		return ret;
	}
	
	public int compareTo(Main o) {
		int f = o.getFitness() - fitness;
		if(f==0)
			return this.getDiff() - o.getDiff();
		return f;
	}
}