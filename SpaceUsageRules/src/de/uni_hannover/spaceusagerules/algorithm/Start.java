/**
 * 
 */
package de.uni_hannover.spaceusagerules.algorithm;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.core.ThreadScheduler;
import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.DataDrawer;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.KML;
import de.uni_hannover.spaceusagerules.io.OSM;

/**
 * TODO Javadoc
 * @author Fabian Pflug
 *
 */
public class Start extends DatasetEntry {

	private static int IMAGEWIDTH = 3840;
	private static int IMAGEHEIGTH = 2160;
	
	/** a target overlap value for all ID's */
	private static final double globalMinOverlap = 0.95;
	
	/** number of maximal running Threads in parallel */
	private static int MAXRUNNING = 1;
	
	/** the truth polygon */
	private Way truth;
	
	/** a minimal overlap value to reach*/
	private double minOverlap;

	/** the output path, to save the images to */
	private static String imagePath = null;
	
	private static boolean images = false;
	
	private static String outputDir = null;

	/** The base path, where the input-data is located.	 */
	public static String path = null;
	
	/**
	 * 
	 * @param backup
	 * @param id
	 */
	public Start(Point backup, String id) {
		super(backup,id);
		this.minOverlap = globalMinOverlap;
		try {
			GeometryFactory gf = new GeometryFactory();
			// try to get better coordinates from the image, because the Data.txt is rounded
			setLocation(gf.createPoint(Image.readCoordinates(new File(path + id + ".jpg"))));
		} catch (Exception e) {
		}
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
		f = new File(imagePath + getID() + ".big.png");
		if(f.exists() && !f.delete())
			System.err.println("Konnte das Bild zu " + getID() + " nicht löschen.");
		
		super.run();

		if(outputDir==null)
			f = new File(path + getID() + ".computed.kml");
		else
			f = new File(outputDir + getID() + ".computed.kml");
		try {
			KML.writeKML(getGuess().getGeometry(), getID(), f);
		} catch (IOException e) {
			System.err.println("Konnte das Lösungspolygon nicht speichern.");
		}

		if(!images)
			return;
		
		this.truth = new Way(KML.loadKML(new File(path + getID() + ".truth.kml")));
		double overlapArea = getGuess().getGeometry().intersection(truth.getGeometry()).getArea();
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
		DataDrawer drawer = new DataDrawer(IMAGEWIDTH,IMAGEHEIGTH,getLocation().getCoordinate(),0.002);
		drawer.render(ways);
		drawer.drawRules(getID() + " enthält " + getRestrictions()  + " und benutzt: " + getUsedRules());
		drawer.drawWay(getGuess(),Color.red);
		drawer.drawWay(truth,Color.green);
		try {
			drawer.saveImage(imagePath + getID() + ".png");
		} catch (IOException e) {
			System.err.println("Konnte das Bild zu " + getID() + " nicht speichern.\n" + imagePath + getID() + ".png");
		}
		drawer = new DataDrawer(IMAGEWIDTH,IMAGEHEIGTH,getLocation().getCoordinate(),0.006);
		drawer.render(ways);
		drawer.drawRules(getID() + " enthält " + getRestrictions()  + " und benutzt: " + getUsedRules());
		drawer.drawWay(truth,Color.green);
		drawer.drawWay(getGuess(),Color.red);
		try {
			drawer.saveImage(imagePath + getID() + ".big.png");
		} catch (IOException e) {
			System.err.println("Konnte das Bild zu " + getID() + " nicht speichern.\n" + imagePath + getID() + ".big.png");
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		OSM.useBuffer(true);
		
		// Siehe auch: https://github.com/arenn/java-getopt/blob/master/gnu/getopt/GetoptDemo.java

		LongOpt[] longopts = new LongOpt[10];

		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longopts[1] = new LongOpt("data", LongOpt.REQUIRED_ARGUMENT, null, 'd');
		longopts[2] = new LongOpt("rules", LongOpt.REQUIRED_ARGUMENT, null, 'r');
		longopts[3] = new LongOpt("image", LongOpt.OPTIONAL_ARGUMENT,null,'i');
		longopts[4] = new LongOpt("overlap", LongOpt.REQUIRED_ARGUMENT, null, 'u');
		longopts[5] = new LongOpt("outputDir",LongOpt.REQUIRED_ARGUMENT,null,'o');
		longopts[6] = new LongOpt("path", LongOpt.REQUIRED_ARGUMENT,null,'p');
		longopts[7] = new LongOpt("threads", LongOpt.REQUIRED_ARGUMENT,null,'t');
		longopts[8] = new LongOpt("width", LongOpt.REQUIRED_ARGUMENT,null,'w');
		longopts[9] = new LongOpt("height", LongOpt.REQUIRED_ARGUMENT,null,'l');

		Getopt g = new Getopt("testprog", args, "hi:d:r:u:o:p:t:w:l:", longopts);
		int c;
		String data = null;
		String rules = null;
		String overlap = null;
		while ((c = g.getopt()) != -1) {
			 switch (c) {
			 case 'h':
				 System.out.println("Es wurde hilfe Angefordert!");
				 return; // nach der Hilfe beenden wir das Program.
			 case 'i':
				 images = true;
				 imagePath = g.getOptarg();
				 if(imagePath!=null) {
					 if(imagePath.charAt(imagePath.length()-1) != '/')
						 imagePath += "/";
					 File f = new File(imagePath);
					 f.mkdirs();
				 }
				 break;
			 case 'd':
				 data = g.getOptarg();
				 break;
			 case 'u':
				 overlap = g.getOptarg();
				 images = true;
				 break;
			 case 'o':
				 outputDir = g.getOptarg();
				 break;
			 case 'r':
				 rules = g.getOptarg();
				 break;
			 case 'p':
				 path = g.getOptarg();
				 break;
			 case 't':
				 MAXRUNNING = Integer.parseInt(g.getOptarg());
				 break;
			 case 'l':
				 IMAGEHEIGTH = Integer.parseInt(g.getOptarg());
				 break;
			 case 'w':
				 IMAGEWIDTH = Integer.parseInt(g.getOptarg());
				 break;
			default:
				 System.out.println("Sorry, I don't understand.");
				 break;
			 }
		}
		if(path==null) {
			if(rules==null || data == null) {
				System.err.println("Es muss ein Pfad oder eine Rules und Data-Datei angegeben werden!");
				return;
			}
			path = new File(data).getParentFile().getAbsolutePath();
		}
		
		File f;
		if(data==null)
			f = new File(path + "Data.txt");
		else
			f = new File(data);
		BufferedReader br = new BufferedReader(new FileReader(f));
		Map<String,Start> instances = new TreeMap<String,Start>();
		String line;
		line = br.readLine();
		int max = Integer.parseInt(line); // 90 for easy
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
		if(rules==null)
			f = new File(path + "Rules.txt");
		else
			f= new File(rules);
		br = new BufferedReader(new FileReader(f));
		while((line = br.readLine()) != null) {
			// Rules can parse the line.
			allRules.add(new Rules(line));
		}
		br.close();

		if(overlap!=null) {
			f = new File(overlap);
			br = new BufferedReader(new FileReader(f));
			while((line = br.readLine()) != null) {
				String[] bla = line.split(",");
				Start s  = instances.get(bla[0].trim());
				if(s != null) 
					s.setMinOverlap(Double.parseDouble(bla[1]));
				}
			br.close();
		}
		ThreadScheduler.schedule(instances.values(), MAXRUNNING);
	}
}
