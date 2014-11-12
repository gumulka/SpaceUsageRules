package de.uni_hannover.spaceusagerules.algorithm;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.Image;
import de.uni_hannover.spaceusagerules.io.OSM;

/**
 * 
 * @author Fabian Pflug
 *
 */
public class DatasetEntry extends Thread{

	/** The base path, where the input-data is located.	 */
	public static String path = "../SpaceUsageRulesVis/assets/";
	
	/** the output path, to save the images to */
	public static String imagePath = "images/";
	
	/** a the parsed rules from the input File */
	public static Set<Rules> allRules = new HashSet<Rules>();
	
	/** the set of restrictions which are processed */
	private Set<String> restrictions;
	
	/**	the location if the dataset */
	private Point location;
	
	/** the ID of the dataset. */
	private String id;
	
	/** the way guessed by the algorithm*/
	private Way guess = null;
	
	/** the OSM objects in the area of this dataset */
	private Collection<Way> ways;
	

	private static GeometryFactory gf = new GeometryFactory();
	
	/**
	 * Initialises a datasetentry. has to provide a coordinate for the location and the ID of the Entry
	 * @param backup the location in the entry
	 * @param id the ID of the entry
	 */
	public DatasetEntry(Point backup, String id) {
		this.restrictions = new TreeSet<String>();
		this.location = backup;
		this.id = id;
		try {
			// try to get better coordinates from the image, because the Data.txt is rounded
			location = gf.createPoint(Image.readCoordinates(new File(path + id + ".jpg")));
		} catch (Exception e) {
		}
	}
	
	/**
	 * returns the id of this dataset (first colum in the input data)
	 * @return the id of the dataset
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * the location of this dataset
	 * @return the location.
	 */
	public Point getLocation() {
		return location;
	}
	
	public Collection<Way> getWays(){
		return ways;
	}
	
	/**
	 * adds a restriction to the set of restrictions for this dataset.
	 * @param restriction a restriction
	 */
	public void addVerbot(String restriction) {
		this.restrictions.add(restriction);
	}
	
	/**
	 * computes the best ruleset to use and with it
	 * the best OSM-object to choose for the restrictions.
	 */
	public void run() {
		
		// compute the best ruleset as defined by the greatest overlap value
		Rules best = null;
		float minRulesOverlap = 0.1f;
		for(Rules r : allRules) {
			float o = r.overlap(restrictions);
			if(o>minRulesOverlap) {
				minRulesOverlap = o;
				best = r;
			}
		}
		// backup. if there is no ruleset applicable, then create the empty ruleset.
		if(best == null) {
			best = new Rules(new TreeSet<String>(), new TreeMap<String,Double>());
		}
		
		System.out.println(id + " benutzt Regelset: " + best);
		
		// compute the best way using the ruleset.
		ways = OSM.getObjectList(location.getCoordinate());
		guess = best.calculateBest(ways, location);
		ways.remove(guess);
	}
	
	/**
	 * returns the Way guessed by our algorithm or null if the run-function did not run yet.
	 * 
	 * @return the way guessed
	 */
	public Way getGuess() {
		return guess;
	}
	
}
