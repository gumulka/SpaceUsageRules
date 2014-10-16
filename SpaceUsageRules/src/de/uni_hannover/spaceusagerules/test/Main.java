package de.uni_hannover.spaceusagerules.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
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
		List<Genetic> gens = new LinkedList<Genetic>();
		for(String s: tags) {
			Genetic g = new Genetic(s);
			g.start();
			gens.add(g);
		}
		for(Genetic g : gens) {
			g.join();
		}
		f = new File("../SpaceUsageRulesVis/assets/Rules.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		for(Genetic g : gens) {
			bw.write(g.getRule());
			bw.write(" -> ");
			bw.write(g.getBest().toString());
			bw.newLine();
		}
		bw.close();
		/*
		List<Double> all = new ArrayList<Double>();
		for(int i = 1;i<=96;i++) {

			String filename = String.format(Locale.GERMAN, "../SpaceUsageRulesVis/assets/%04d.jpg",i);
    		InputStream ins = new FileInputStream(new File(filename));
    		Coordinate location = Image.readCoordinates(ins);
    		filename = String.format(Locale.GERMAN, "../SpaceUsageRulesVis/assets/%04d.truth.kml",i);
    		Polyline p = KML.loadKML(new File(filename));
    		if(p.isArea())
    			all.add(location.distanceTo(p.getPoints()));
		}
		Collections.sort(all);
		for(Double d : all)
			System.out.println(d);
			
		// */
	}

}
