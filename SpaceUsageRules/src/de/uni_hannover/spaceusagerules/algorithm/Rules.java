package de.uni_hannover.spaceusagerules.algorithm;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.uni_hannover.spaceusagerules.core.Way;
/**
 * represents our rules and the related restrictions.
 * 
 * \latexonly (see also \fref{sec:Regeln}) \endlatexonly
 * 
 * @author Fabian Pflug
 *
 */
public class Rules{

	/** a set of all the restrictions. */
	protected Collection<String> restrictions;
	/** the rules as "osm-key - osm-value to [0..2]"	 */
	protected Map<String,Double> weights;
	
	/** a offset to add to the distance of all polygons before weighting them.
	 * 
	 * the value is found using some test with different magic numbers. from 0.1 to 0.00001.
	 */
	private static final double OFFSET = 0.0002;
	
	/**
	 * initializes with an empty set of rules and restictions.
	 */
	public Rules() {
		this.restrictions = new TreeSet<String>();
		this.weights = new TreeMap<String,Double>();
	}
	
	/**
	 * parses a line into a valid representation of Rules.
	 * @param line a string describing a line of rules.
	 * \latexonly as defined in \fref{sec:Eingabedaten_Wir} \endlatexonly
	 */
	public Rules(String line) {
		String verbote = line.substring(line.indexOf('[')+1, line.indexOf(']'));
		String rules = line.substring(line.lastIndexOf('[')+1, line.lastIndexOf(']'));
		String[] v = verbote.split(",");
		restrictions = new TreeSet<String>();
		for(String s : v)
			restrictions.add(s.trim());
		weights = new TreeMap<String, Double>();
		for(String s : rules.split(",")) {
			String[] bla = s.split("->");
			weights.put(bla[0].trim(), Double.parseDouble(bla[1]));
		}
	}
	
	
	/**
	 *  Initializes with the given rules and restrictions.
	 * @param restrictions a set of restrictions.
	 * @param rules a set of rules as "osm-key - osm-value to [0..2]"
	 */
	public Rules(Collection<String> restrictions, Map<String,Double> rules) {
		this.restrictions = restrictions;
		this.weights = rules;
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
		return best;
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
		return restrictions.toString() + " -> " + weights.toString();
	}

}
