package de.uni_hannover.spaceusagerules.algorithm;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.DataDrawer;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.KML;
import de.uni_hannover.spaceusagerules.io.OSM;

/**
 * 
 * 
 * @author Fabian Pflug
 *
 */
public class Start extends Thread{

	/** The base path, where the input-data is located.	 */
	private static String path = "../SpaceUsageRulesVis/assets/";
	/** the output path, to save the images to */
	private static String imagePath = "images/";
	/** a the parsed rules from the input File */
	private static Set<Rules> allRules = new HashSet<Rules>();
	/** a target overlap value for all ID's */
	private static final double globalMinOverlap = 0.85;
	/** number of maximal running Threads in parallel */
	private static final int MAXRUNNING = 1;
	
	/**  */
	private Set<String> verbote;
	private Coordinate location;
	private String id;
	private Way truth;
	private double minOverlap;
	private Way guess;
	
	public Start(Coordinate backup, String id) {
		this.verbote = new TreeSet<String>();
		this.location = backup;
		this.id = id;
		this.truth = new Way();
		try {
			InputStream is = new FileInputStream(new File(path + id + ".jpg"));
			location = Image.readCoordinates(is);
		} catch (Exception e) {
		}
		this.minOverlap = globalMinOverlap;
	}
	
	public void addVerbot(String verbot) {
		this.verbote.add(verbot);
	}
	
	public void generateImage(Collection<Way> ways) {
		DataDrawer drawer = new DataDrawer(3840,2160,location);
		drawer.render(ways);
		drawer.drawWay(truth,Color.green);
		drawer.drawWay(guess,Color.pink);
		System.err.println("Anforderungen an " + id + " nicht geschafft.");
		try {
			drawer.saveImage(imagePath + id + ".png");
		} catch (IOException e) {
			System.err.println("Konnte das Bild zu " + id + " nicht speichern.\n" + imagePath + id + ".png");
		}
	}
	
	
	public void run() {
		File f = new File(imagePath + id + ".png");
		if(f.exists() && !f.delete())
			System.err.println("Konnte das Bild zu " + id + " nicht löschen.");
		
		
		Rules best = null;
		float minRulesOverlap = 0.1f;
		for(Rules r : allRules) {
			float o = r.overlap(verbote);
			if(o>minRulesOverlap) {
				minRulesOverlap = o;
				best = r;
			}
		}
		if(best == null) {
			best = new Rules(new TreeSet<String>(), new TreeMap<String,Double>());
		}
		
		System.out.println(id + " benutzt Regelset: " + best);
		
		Set<Way> ways = OSM.getObjectList(location, 0.0005f);
		
		guess = best.calculateBest(ways, location);
		ways.remove(guess);
		truth.addAllCoordinates(KML.loadKML(new File(path + id + ".truth.kml")).getPoints());
		
		double overlapArea = guess.getPolyline().boundingBoxOverlapArea(truth.getPolyline());
		overlapArea = Math.min(overlapArea/truth.getPolyline().boundingBoxArea(), overlapArea/guess.getPolyline().boundingBoxArea());
		if(overlapArea>minOverlap)
			return;
		guess.addOriginalTag("InMa_Overlap", "" + overlapArea);
		generateImage(ways);
	}
	
	public void setMinOverlap(double overlap) {
		this.minOverlap = overlap;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		OSM.useBuffer(true);
		
		File f = new File(path + "Data.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		Map<String,Start> instances = new TreeMap<String,Start>();
		String line;
		line = br.readLine();
		int max = 90; // Integer.parseInt(line);
		for(int i = 0; i<max;i++) {
			line = br.readLine();
			if(line == null)
				break;
			String[] bla = line.split(",");
			String id = bla[0].trim();
			if(instances.containsKey(id)) {
				Start a = instances.get(id);
				a.addVerbot(bla[3].trim());
			}
			else {
				Coordinate backup = new Coordinate(Double.parseDouble(bla[1]), Double.parseDouble(bla[2]));
				Start s = new Start(backup, id);
				s.addVerbot(bla[3].trim());
				instances.put(id, s);
			}			
		}
		br.close();

		f = new File(path + "Rules.txt");
		br = new BufferedReader(new FileReader(f));
		while((line = br.readLine()) != null) {
			String verbote = line.substring(line.indexOf('[')+1, line.indexOf(']'));
			String rules = line.substring(line.lastIndexOf('[')+1, line.lastIndexOf(']'));
			String[] v = verbote.split(",");
			Set<String> verbooote = new TreeSet<String>();
			for(String s : v)
				verbooote.add(s.trim());
			Map<String,Double> ruules = new TreeMap<String, Double>();
			for(String s : rules.split(",")) {
				String[] bla = s.split("->");
				ruules.put(bla[0].trim(), Double.parseDouble(bla[1]));
			}
			allRules.add(new Rules(verbooote,ruules));
		}
		br.close();

		f = new File(path + "Overlap.txt");
		br = new BufferedReader(new FileReader(f));
		while((line = br.readLine()) != null) {
			String[] bla = line.split(",");
			Start s  = instances.get(bla[0].trim());
			if(s != null) 
				s.setMinOverlap(Double.parseDouble(bla[1]));
		}
		br.close();
		
		List<Start> running = new LinkedList<Start>();
		for(Start s : instances.values()) {
			while(true) {
				if(running.size()<MAXRUNNING) {
					running.add(s);
					s.start();
					break;
				} else
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				for(Start r : running)
					if(!r.isAlive()) {
						running.remove(r);
						break;
					}
			}
		}
		for(Start s : instances.values())
			try {
				s.join();
			} catch (InterruptedException e) {
			}
			
		for(Entry<String,Start> id : instances.entrySet()) {
			f = new File(path + id.getKey() + ".computed.kml");
			KML.writeKML(id.getValue().getGuess().getPolyline(), id.getKey(), f);
			
		}
		
	}
	
	public Way getGuess() {
		return guess;
	}
	
}
