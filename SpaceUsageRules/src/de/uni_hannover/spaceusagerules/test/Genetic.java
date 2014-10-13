package de.uni_hannover.spaceusagerules.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Image;
import de.uni_hannover.spaceusagerules.core.KML;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Polyline;
import de.uni_hannover.spaceusagerules.core.Way;

public class Genetic extends Thread{

	private List<Polyline> truths;
	private List<Coordinate> starting;
	private List<List<Way>> possebilities;
	private List<Population> pops, nextGen;
	private String suche;
	
	public Genetic(String sign) throws Exception {
		suche = sign;
		truths = new ArrayList<Polyline>();
		starting = new ArrayList<Coordinate>();
		possebilities = new ArrayList<List<Way>>();
		pops = new ArrayList<Population>();
		nextGen = new ArrayList<Population>();

		for(int i = 0; i<200; i++) {
			pops.add(new Population());
		}
		
		File f = new File("../SpaceUsageRulesVis/assets/Data.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		br.readLine();
		while((line = br.readLine()) != null) {
			String[] bla = line.split(",");
			if(bla[3].trim().equals(sign)) {
				int i = Integer.parseInt(bla[0]);
				String filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%04d.jpg",i);
				InputStream is = new FileInputStream(new File(filename));
				Coordinate c = Image.readCoordinates(is); 
				starting.add(c);
				filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%04d.truth.kml",i);
				truths.add(KML.loadKML(new File(filename)));
				possebilities.add(OSM.getObjectList(c));
			}
		}
		br.close();
	}
	
	private void calcFitness() {
		for(Population p : pops) {
			p.calcFitness(truths, possebilities, starting);
		}
	}
	
	private void nextGen() throws IOException {
		Collections.sort(pops);
		for(int i = 0; i<50; i++) {
			nextGen.add(pops.get(i));
		}
		Random r = new Random();
		for(int i = 0; i<80; i++) {
			int a = r.nextInt(80);
			int b = r.nextInt(80);
			nextGen.add(pops.get(a).recombine(pops.get(b)));
		}
		for(int i = 0; i<70; i++) {
			nextGen.add(new Population());
		}
		pops = nextGen;
		nextGen = new ArrayList<Population>();
	}
	
	public void run() {
		for(int i = 0; i<1000; i++) {
			calcFitness();
			try {
				nextGen();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(suche + " -> " + pops.get(0));
	//	System.out.println(new Date());
	}

	
	
}
