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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import de.uni_hannover.spaceusagerules.io.RulesetIO;

/**
 * The main class for our application. An instance is responsible for one ID in the read in dataset.
 * 
 * @author Fabian Pflug
 *
 */
public class Start extends DatasetEntry {

	/** the width of the image to create */
	private static int imageWidth = 3840;
	/** the height of the image to create */
	private static int imageHeight = 2160;
	
	/** a target overlap value for all ID's */
	private static final double GLOBALMINOVERLAP = 0.95;
	
	/** number of maximal running Threads in parallel */
	private static int maxRunning = 1;
	
	/** the truth polygon */
	private Way truth;
	
	/** a minimal overlap value to reach*/
	private double minOverlap;

	/** the output path, to save the images to */
	public static String imagePath = null; // XXX private machen.
	
	/** of it is set, then images will be created */
	public static boolean images = false; // XXX private machen.
	
	/** the directory to write the generated files to */
	private static String outputDir = null;

	/** The base path, where the input-data is located.	 */
	public static String path = null;
	
	/**
	 * creates an entry in the dataset and tries to read in the coordinates from the image belonging to it.
	 * @param backup a backup coordinate if it is not possible to read metadata from the image.
	 * @param id the id of this dataset.
	 */
	public Start(Point backup, String id) {
		super(backup,id);
		this.minOverlap = GLOBALMINOVERLAP;
		try {
			GeometryFactory gf = new GeometryFactory();
			// try to get better coordinates from the image, because the Data.txt is rounded
			Point im = gf.createPoint(Image.readCoordinates(new File(path + id + ".jpg")));
			if(im.distance(backup)>0.0001)
				System.out.println("ignoring image metadata, because distance to high. " + im.distance(backup));
			else
				setLocation(im);
		} catch (Exception e) {
		}
	}


	/**
	 * Sets the minimum overlap to reach so that no image is created.
	 * @param overlap the overlap value
	 */
	public void setMinOverlap(double overlap) {
		this.minOverlap = overlap;
	}
	
	
	/**
	 * the worker method, which handels the IO and calculations for this datasetEntry 
	 */
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

		if(!images) //if no images are wanted, we can stop here.
			return;
		File t = new File(path + getID() + ".truth.kml");
		if(!t.exists() ||!t.canRead()) {
			System.err.println("Fehler beim einlesen der truth datei.\n" + t.getAbsolutePath());
			return;
		}
		this.truth = new Way(KML.loadKML(t));
		double overlapArea = getGuess().getGeometry().intersection(truth.getGeometry()).getArea();
		overlapArea = Math.min(overlapArea/truth.getArea(), overlapArea/getGuess().getArea());
		if(overlapArea>minOverlap)
			return;
		getGuess().addOriginalTag("InMa_Overlap", "" + overlapArea);
		System.err.println("Anforderungen an " + getID() + " nicht geschafft.");
		generateImage();
	}
	
	/**
	 * generates two images for this entry with colored lines for the truth and guess polygon.
	 * one is zoomed in, the other one shows a wider perspective
	 */
	public void generateImage() {
		DataDrawer drawer = new DataDrawer(imageWidth,imageHeight,getLocation().getCoordinate(),0.002);
		drawer.render(getWays());
		drawer.drawRules(getID() + " enthält " + getRestrictions()  + " und benutzt: " + getUsedRules());
		drawer.drawWay(getGuess(),Color.red);
		drawer.drawWay(truth,Color.green);
		try {
			drawer.saveImage(imagePath + getID() + ".png");
		} catch (IOException e) {
			System.err.println("Konnte das Bild zu " + getID() + " nicht speichern.\n" + imagePath + getID() + ".png");
		}
		drawer = new DataDrawer(imageWidth,imageHeight,getLocation().getCoordinate(),0.006);
		drawer.render(getWays());
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
	 * prints out a help message
	 */
	public static void printhelp() {
		System.out.println("usage: name <parameter [arg]> ");
		System.out.println("");
		System.out.println("Mögliche Parameter sind:");
		System.out.println("  -h  --help      gibt diese Hilfe aus.");
		System.out.println("  -d  --data      Pfad zur Datei mit Verboten und Coordinaten");
		System.out.println("  -r  --rules     Pfad zur Datei mit den Regeln");
		System.out.println("  -t  --threads   Anzahl der Threads in Parallel");
		System.out.println("  -u  --overlap   Pfad zur Datei mit Overlap-Werten welche erreicht werden sollen.");
		System.out.println("  -o  --outputDir Pfad zu einem Ordner, in welchem die Ausgabedateien gespeichert werden sollen.\n" +
		 		"\t\t  Die *.computet.kml Datein\n" + 
			 	"\t\t  Die Bilddateien (Wird von -i überschrieben)");
		System.out.println("  -p  --path      Pfad zu einem Ordner auf dem Dateisystem, in welchem die folgenden Dateien liegen:\n" +
		 		"\t\t  Data.txt (wird von -d überschrieben)\n" +
		 		"\t\t  Rules.xml (wird von -r überschrieben)\n" +
		 		"\t\t  Overlap.txt (Optional, wird von -u überschrieben)");
		System.out.println("  -i  --image     Gibt an, dass Bilder erstellt werden sollen und ein optinales Ausgabeverzeichnis.");
		System.out.println("  -l  --height    Die Höhe des zu erstellenden Bildes (" + imageHeight + " ist default.)");
		System.out.println("  -w  --width     Die Breite des zu erstellenden Bildes (" + imageWidth + " ist default.)");
	}
	
	/**
	 * the main starting point for our application.
	 * 
	 * @param args the command line parameters.
	 * @throws IOException the error thrown if there are problems reading the nesessary files.
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
		g.setOpterr(false); // We'll do our own error handling
		int c;
		String data = null;
		String rules = null;
		String overlap = null;
		while ((c = g.getopt()) != -1) {
			 switch (c) {
			 case 'h':
				 printhelp();
				 return; // nach der Hilfe beenden wir das Program.
			 case 'i': // --image
				 images = true;
				 String ipath = g.getOptarg();
				 if(ipath!=null) {
					 if(ipath.charAt(ipath.length()-1) != '/')
						 ipath += "/";
					 File f = new File(ipath);
					 f.mkdirs();
					 imagePath = ipath;
				 }
				 break;
			 case 'd': // --data
				 data = g.getOptarg();
				 break;
			 case 'u': // --overlap
				 overlap = g.getOptarg();
				 images = true;
				 break;
			 case 'o': // --outputDir
				 outputDir = g.getOptarg();
				 if(outputDir!=null) {
					 if(outputDir.charAt(outputDir.length()-1) != '/')
						 outputDir += "/";
					 File f = new File(outputDir);
					 f.mkdirs();
				 }
				 if(imagePath == null)
					 imagePath = outputDir;
				 break;
			 case 'r': // --rules
				 rules = g.getOptarg();
				 break;
			 case 'p': // --path
				 path = g.getOptarg();
				 break;
			 case 't': // --threads
				 maxRunning = Integer.parseInt(g.getOptarg());
				 break;
			 case 'l': // --height
				 imageHeight = Integer.parseInt(g.getOptarg());
				 break;
			 case 'w': // --width
				 imageWidth = Integer.parseInt(g.getOptarg());
				 break;
			default: // error
				 System.err.println("Falscher Parameter: " + (char) g.getOptopt());
				 printhelp();
				 return;
			 }
		}
		if(path==null) {
			if(rules==null || data == null) {
				System.err.println("Es muss ein Pfad oder eine Rules und Data-Datei angegeben werden!");
				printhelp();
				return;
			}
			path = new File(data).getParentFile().getAbsolutePath();
		}
		
		File f;
		if(data==null)
			f = new File(path + "Data.txt");
		else
			f = new File(data);
		if(!f.exists()) {
			System.err.println("Rules-Datei existiert nicht.");
			return;
		}
			
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
		
		// read the ruleset from file and parse from XML format
		if(rules==null)
			f = new File(path + "Rules.xml");
		else
			f= new File(rules);
		if(!f.exists()) {
			System.err.println("Rules file does not exist!");
			return;
		}	
		//reading and parsing is done in RulesetIO
		allRules = new HashSet<Rules>(RulesetIO.readRules(f));
		
		
		if(overlap==null)
			f = new File(path + "Overlap.txt");
		else
			f= new File(overlap);
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
		
		
		ThreadScheduler.schedule(instances.values(), maxRunning);
		
		
//		List<Double> bla = OSM.size;
//		Collections.sort(bla);
//		buckets(10, bla.get(0), bla.get(bla.size()*6/10), bla);
		// */
	}
	
	public static void buckets(int n,double min, double max, List<Double> list) {
		int[] buckets = new int[n];
		for(int i = 0; i<n; i++){
			buckets[i] = 0;
		}
		double section = (max-min)/n;
		for(double d : list) {
			for(int i = 0; i<n;i++) {
				if(d < (min + (i+1)*section)) {
					buckets[i]++;
					break;
				}
			}
		}
		for(int i = 0; i<n;i++) {
			System.out.println("" + (min+i*section) + " - " + buckets[i]);
		}
	}
}
