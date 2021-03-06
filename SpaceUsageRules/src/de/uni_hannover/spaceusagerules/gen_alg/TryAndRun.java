package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.uni_hannover.spaceusagerules.algorithm.DatasetEntry;
import de.uni_hannover.spaceusagerules.algorithm.Rules;
import de.uni_hannover.spaceusagerules.algorithm.Start;
import de.uni_hannover.spaceusagerules.core.ThreadScheduler;
import de.uni_hannover.spaceusagerules.io.OSM;

public class TryAndRun {

	public static void main(String[] args) throws Exception, IOException {
		OSM.useBuffer(true);
		Start.images = true;
		Start.path = new File("../Testdatensatz/");
		Start.imagePath = "images/";

		File f;
		f = new File(Start.path, "Data.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		Map<String, Start> instances = new TreeMap<String, Start>();
		String line;
		line = br.readLine();
		int max = Integer.parseInt(line); // 90 for easy
		GeometryFactory gf = new GeometryFactory();
		for (int i = 0; i < max; i++) {
			line = br.readLine();
			if (line == null)
				break;
			String[] bla = line.split(",");
			String id = bla[0].trim();
			if (instances.containsKey(id)) {
				Start a = instances.get(id);
				a.addVerbot(bla[3].trim());
			} else {
				Coordinate backup = new Coordinate(Double.parseDouble(bla[2]),
						Double.parseDouble(bla[1]));
				Start s = new Start(gf.createPoint(backup), id);
				s.addVerbot(bla[3].trim());
				instances.put(id, s);
			}
		}
		br.close();


		f = new File(Start.path, "Overlap.txt");
		if(f.exists() && f.canRead()) {
			br = new BufferedReader(new FileReader(f));
			while((line = br.readLine()) != null) {
				String[] bla = line.split(",");
				Start s  = instances.get(bla[0].trim());
				if(s != null) 
					s.setMinOverlap(Double.parseDouble(bla[1]));
				}
			br.close();
		}

		Population.GENERATOR = 16;
		Main test = new Main(500, 200, 6, 3, 64);
		Main.prepare();
		test.run();
		test.writeout();
		DatasetEntry.allRules = new TreeSet<Rules>();
		for (Genetic g : test.allGens) {
			Population r = g.getBest();
			for (String s : g.getRule().split(","))
				r.addRestriction(s.trim());
			DatasetEntry.allRules.add(g.getBest());
		}
		ThreadScheduler.schedule(instances.values(), 4);

		double overlaping = 0;
		for(Start s : instances.values()) {
			overlaping += s.overlapArea;
		}
		System.out.println("Overlap Area: " + overlaping/instances.size());
	}
	
}
