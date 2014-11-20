package de.uni_hannover.spaceusagerules.algorithm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.RulesetIO;
/**
 * represents our rules and the related restrictions.
 * 
 * \latexonly (see also \fref{sec:Regeln}) \endlatexonly
 * 
 * @author Fabian Pflug and Peter Zilz
 *
 */
public class Rules{

	/** a set of all the restrictions. */
	protected Collection<String> restrictions;
	/** the rules as "osm-key - osm-value to [0..2]"	 */
	protected Map<String,Double> weights;
	
	/**
	 * maps names of single OSM-tags to pairs of threshold and radius
	 */
	protected Map<String,double[]> thresholds;
	
	/** a offset to add to the distance of all polygons before weighting them.
	 * 
	 * the value is found using some test with different magic numbers. from 0.1 to 0.00001.
	 */
	private static final double OFFSET = 0.0002;
	
	/**
	 * Initializes with an empty set of rules, restictions and thresholds.
	 */
	public Rules() {
		this.restrictions = new TreeSet<String>();
		this.weights = new TreeMap<String,Double>();
		this.thresholds = new TreeMap<String,double[]>();
	}
	
	/**
	 * parses a line into a valid representation of Rules.
	 * The input string must have the following form:<BR>
	 * [SUR,SUR,...] -> [rules how to weight, ...] -> [threshold:42] -> [radius:10]<BR>
	 * Better use {@link RulesetIO#readRules(String)} to parse.
	 * @param line a string describing a line of rules.
	 * \latexonly as defined in \fref{sec:Eingabedaten_Wir} \endlatexonly
	 */
	@Deprecated
	public Rules(String line) {
		this();
		//first block contains the list of SURs
		int blockstart = line.indexOf('['); //begin of the block incl. '['
		int blockend = line.indexOf(']'); //end of the block excl. ']'
		String verbote = line.substring(blockstart+1, blockend);
		String[] v = verbote.split(",");
		for(String s : v)
			restrictions.add(s.trim());
		
		//second block contains the list of weighing rules
		blockstart = line.indexOf('[', blockend+1);
		blockend = line.indexOf(']', blockstart);
		String rules = line.substring(blockstart+1, blockend);
		for(String s : rules.split(",")) {
			String[] bla = s.split("->");
			weights.put(bla[0].trim(), Double.parseDouble(bla[1]));
		}
		
	}
	
	/**
	 *  Initializes with the given rules and restrictions.
	 * @param restrictions a set of restrictions. 
	 * @param rules a set of rules as "osm-key - osm-value to [0..2]"
	 * @param thresholds map from singel SUR names to pairs of threshold and radius 
	 */
	public Rules(Collection<String> restrictions, Map<String,Double> rules, Map<String,double[]> thresholds) {
		this.restrictions = restrictions;
		this.weights = rules;
		this.thresholds = thresholds;
	}
	

	/**
	 * calculates the weighted distance from a given coordinate to the given way. 
	 * @param c the coordinate to calculate the distance from
	 * @param w the OSM-object to calculate the distance to
	 * @return the weighted distance by tag and rules
	 */
	public double calcDist(Point c, Way w) {
		double distance = w.distanceTo(c);
		w.addOriginalTag("InMa_preDistance", "" + distance);
		distance += OFFSET;
		String combine;
		// iterate over all Tags in the OSM object
		for(String s : w.getTags().keySet()) {
			combine = s.trim() + " - " + w.getValue(s).trim();
			// check for key + value
			if(weights.keySet().contains(combine)) {
				distance *= weights.get(combine);
			}
			// and just key
			if(weights.keySet().contains(s)) {
				distance *= weights.get(s);
			}
		}
		return distance;
	}
	
	/**
	 * calculates the way with the nearest weighted distance to the given location.
	 * @param ways a collection of ways to check.
	 * @param location the starting location
	 * @return a way from ways
	 */
	public Way calculateBest(Collection<Way> ways, Point location) {
		Way best = null;
		double distance = Double.MAX_VALUE;
		double d;
		for(Way w : ways) {
			// we only look at areas at the moment, because we want to have a polygon, no polyline.
			if(!w.isPolygon())
				continue;
			d = calcDist(location, w);
			// this is used later in DataDrawer to print out the distance to the user.
			w.addOriginalTag("InMa_Distance", "" + d);
			// get the Object with the lowest distance, or if equal, the smaller one.
			if(d<distance || (d==distance && w.getArea() < best.getArea())) {
				best = w;
				distance = d;
			}
		}
		
		//check if any thresholds are exceeded and shrink if necessary
		best = considerThresholds(best, location);
		
		return best;
	}
	
	/**
	 * Tests if any thresholds were exceeded and shrinks the geometry if necessary.
	 * From the set of all exceeded thresholds, the lowest radius is selected. Then the
	 * polygon is intersected with a regular octagon. If no thresholds were exceeded the
	 * geometry remains unchanged.
	 * @param best contains the guessed polygon
	 * @param location where it all takes place
	 * @return either the same object or a new one with smaller size
	 */
	private Way considerThresholds(Way best, Point location){
		
		Geometry guessed = best.getGeometry();
		
		double area = guessed.getArea();
		
		double thr;
		List<String> consider = new Vector<String>();
		//iterate over all given SURs
		for(String sur : thresholds.keySet()){
			thr = thresholds.get(sur)[0];
			//if the area is greater than a threshold, keep that SUR in mind
			if(thr < area )
				consider.add(sur);
		}
		
		//if no threshold was exceeded, return the geometry unchanged
		if(consider.isEmpty())
			return best;
		
		//search for the minimal radius of those SURs, whose thresholds were exceeded
		double minimalRadius = Double.MAX_VALUE;
		for(String sur : consider){
			//thr is now a radius
			thr = thresholds.get(sur)[1];
			if(thr < minimalRadius){
				minimalRadius = thr;
			}
		}
		
		//intersect with regular neighborhood
		Geometry neighborhood = Rules.createNgon(8, minimalRadius, location);
		Geometry intersected = neighborhood.intersection(guessed);

		Way output = new Way(intersected);
		return output;
	}
	
	/**
	 * Creates a regular polygon with n vertices. Is is created by rotating, starting at
	 * (radius,0) relative to the given center.
	 * @param n number of vertices
	 * @param radius distance from the vertices to the center
	 * @param center center around which the polygon is created
	 * @return regular polygon
	 */
	public static Geometry createNgon(int n, double radius, Point center){
		
		Coordinate[] cList = new Coordinate[n+1];
		
		Coordinate c = center.getCoordinate();
		
		//create the N-gon by rotating around center.
		double x,y;
		for(int i=0;i<n;i++){
			x = radius*Math.cos(i*2./n*Math.PI)+c.x;
			y = radius*Math.sin(i*2./n*Math.PI)+c.y;
			cList[i] = new Coordinate(x,y);
		}
		
		//first and last point have to be the same, to get a closed line.
		//but not the same object
		cList[n] = new Coordinate(cList[0].x, cList[0].y);
		
		//create the outline of the polygon
		LinearRing ring = new GeometryFactory().createLinearRing(cList);
		
		//second argument contains the holes in this polygon. There are none.
		Geometry out = new Polygon(ring, null, new GeometryFactory());
		
		return out;
	}
	
	/**
	 * calculates the overlap of this collection of restrictions with another. 
	 * @param v a collection of restrictions
	 * @return a value between 0 and 1, defining the overlap or 0.1 if this Rules has no restrictions.
	 */
	public float overlap(Collection<String> v) {
		if(restrictions.size()==0)
			return 0.1f;
		if(v.size()==0)
			return 0.0f;
		float start = 1, diff = .5f / restrictions.size();
		for(String s : restrictions) {
			if(!v.contains(s))
				start -= diff;
		}
		diff= .5f /v.size();
		for(String s : v) {
			if(!restrictions.contains(s))
				start -= diff;
		}
		return start;
	}
	
	/**
	 * returns the rules to print out
	 * \latexonly as defined in \fref{sec:Eingabedaten_Wir} \endlatexonly
	 */
	public String toString() {
		return restrictions.toString() + " -> " + weights.toString() + " -> " + thresholds.toString();
	}

	public Collection<String> getRestrictions() {
		return restrictions;
	}

	public Map<String, Double> getWeights() {
		return weights;
	}

	public Map<String, double[]> getThresholds() {
		return thresholds;
	}

}
