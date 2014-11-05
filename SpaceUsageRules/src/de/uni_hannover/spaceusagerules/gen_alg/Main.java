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

import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.OSM;

/**
 * Klasse, welche für jede SpaceUsageRule aus dem Testdatensatz einen genetischen Algorithmus erstellt und diesen durchlaufen lässt.
 */
public class Main {

	public static final int MAXTHREADS = 3;
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws ImageProcessingException
	 */
	public static void main(String[] args) throws Exception,
			IOException {
		OSM.useBuffer(true);
		long startTime = System.currentTimeMillis();
		Set<String> possibilities = new TreeSet<String>();
		Map<String,Set<String>> tags = new TreeMap<String,Set<String>>();
		File f = new File("../SpaceUsageRulesVis/assets/Data.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String lastID = null;
		String tag = "";
		String line;
		line = br.readLine();
		int max = Integer.parseInt(line);
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
						possibilities.add(e.getKey() + " - " + e.getValue());
						possibilities.add(e.getKey());
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
				tag = bla[3] + ", ";
				lastID = bla[0];
			}
		}
		br.close();
		
		System.out.println(tags.size() + " verschiedene Tags.");
		List<Genetic> allGens = new ArrayList<Genetic>();
		for(Entry<String,Set<String>> e: tags.entrySet())
			allGens.add(new Genetic(e.getKey(),e.getValue(), possibilities));
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
			g.join();
		}
		f = new File("../SpaceUsageRulesVis/assets/Rules.generatet.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		for(Genetic g : gens) {
			bw.write("[ " + g.getRule());
			bw.write("] -> ");
			bw.write(g.getBest().toString());
			bw.newLine();
		}
		bw.close();
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("Laufzeit: " + diff/1000 + " Sekunden.");
		System.out.println("Laufzeit: " + diff/3600000 + " Stunden.");
		System.out.println(new Date());
	}

}