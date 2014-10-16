package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import de.uni_hannover.spaceusagerules.core.Image;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Way;

public class Generator {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Set<String> possibilities = new TreeSet<String>();
		OSM.useBuffer(true);
		
		for(int i = 1; i<=96;i++) {
			String filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%04d.jpg",i);
			InputStream is = new FileInputStream(new File(filename));
			for(Way w : OSM.getObjectList(Image.readCoordinates(is))) {
				for(Entry<String,String> e : w.getTags().entrySet()) {
					possibilities.add(e.getKey() + " - " + e.getValue());
					possibilities.add(e.getKey());
				}
			}
		}
		File f = new File("possibilities.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		for(String s : possibilities) {
			bw.write(s);
			bw.newLine();
		}
		bw.close();
	}

}
