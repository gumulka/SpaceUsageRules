package de.uni_hannover.spaceusagerules.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import com.drew.imaging.ImageProcessingException;

import de.uni_hannover.spaceusagerules.core.OSM;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ImageProcessingException
	 */
	public static void main(String[] args) throws Exception,
			IOException {
		OSM.useBuffer(true);
		Set<String> tags = new TreeSet<String>();

		File f = new File("../SpaceUsageRulesVis/assets/Data.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] bla = line.split(",");
			tags.add(bla[3].trim());
		}
		br.close();
		System.out.println(tags.size() + " verschiedene Tags.");
		for(String s: tags)
			new Genetic(s).start();
	}

}
