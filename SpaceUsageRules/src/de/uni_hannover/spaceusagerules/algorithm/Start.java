/**
 * 
 */
package de.uni_hannover.spaceusagerules.algorithm;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.DataDrawer;
import de.uni_hannover.spaceusagerules.io.KML;
import de.uni_hannover.spaceusagerules.io.OSM;

/**
 * @author Fabian Pflug
 *
 */
public class Start extends DatasetEntry {

	private static final int IMAGEWIDTH = 3840;
	private static final int IMAGEHEIGTH = 2160;
	
	/** a target overlap value for all ID's */
	private static final double globalMinOverlap = 0.85;
	
	/** number of maximal running Threads in parallel */
	private static final int MAXRUNNING = 1;
	
	/** the truth polygon */
	private Way truth;
	
	/** a minimal overlap value to reach*/
	private double minOverlap;
	
	/**
	 * 
	 * @param backup
	 * @param id
	 */
	public Start(Point backup, String id) {
		super(backup,id);
		this.minOverlap = globalMinOverlap;
	}


	/**
	 * Sets the minimum overlap to reach so that no image is created.
	 * @param overlap the overlap value
	 */
	private void setMinOverlap(double overlap) {
		this.minOverlap = overlap;
	}
	
	
	public void run() {
		File f = new File(imagePath + getID() + ".png");
		if(f.exists() && !f.delete())
			System.err.println("Konnte das Bild zu " + getID() + " nicht löschen.");
		
		super.run();
		
		this.truth = new Way(KML.loadKML(new File(path + getID() + ".truth.kml")));
		
//		double overlapArea = getGuess().getPolyline().boundingBoxOverlapArea(truth.getPolyline());
		double overlapArea = getGuess().getGeometry().getEnvelopeInternal().intersection(truth.getGeometry().getEnvelopeInternal()).getArea();
		overlapArea = Math.min(overlapArea/truth.getArea(), overlapArea/getGuess().getArea());
		if(overlapArea>minOverlap)
			return;
		getGuess().addOriginalTag("InMa_Overlap", "" + overlapArea);
		System.err.println("Anforderungen an " + getID() + " nicht geschafft.");
		generateImage(getWays());

	}
	
	/**
	 * generates an Image 
	 * @param ways
	 */
	public void generateImage(Collection<Way> ways) {
		DataDrawer drawer = new DataDrawer(IMAGEWIDTH,IMAGEHEIGTH,getLocation().getCoordinate());
		drawer.render(ways);
		drawer.drawWay(truth,Color.green);
		drawer.drawWay(getGuess(),Color.pink);
		try {
			drawer.saveImage(imagePath + getID() + ".png");
		} catch (IOException e) {
			System.err.println("Konnte das Bild zu " + getID() + " nicht speichern.\n" + imagePath + getID() + ".png");
		}
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
		GeometryFactory gf = new GeometryFactory();
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
				Coordinate backup = new Coordinate(Double.parseDouble(bla[2]), Double.parseDouble(bla[1]));
				Start s = new Start(gf.createPoint(backup), id);
				s.addVerbot(bla[3].trim());
				instances.put(id, s);
			}			
		}
		br.close();

		// read in the Rules
		f = new File(path + "Rules.txt");
		br = new BufferedReader(new FileReader(f));
		while((line = br.readLine()) != null) {
			// Rules can parse the line.
			allRules.add(new Rules(line));
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
			KML.writeKML(id.getValue().getGuess().getGeometry(), id.getKey(), f);
			
		}
		
	}
}
