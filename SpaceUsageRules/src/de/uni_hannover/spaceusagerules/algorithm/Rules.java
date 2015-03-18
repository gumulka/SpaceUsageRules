package de.uni_hannover.spaceusagerules.algorithm;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
//		w.addOriginalTag("InMa_preDistance", "" + distance);
		distance += OFFSET;
		String combine;
		// iterate over all Tags in the OSM object
		Set<String> keySet = weights.keySet();
		for(Entry<String,String> s : w.getTags().entrySet()) {
			combine = s.getKey().trim() + " - " + s.getValue().trim();
			
			// check for key + value
			if(keySet.contains(combine)) {
				distance *= weights.get(combine);
			}
			
			// and just key
			if(keySet.contains(s.getKey())) {
				distance *= weights.get(s.getKey());
			}
			
		}
//		w.addOriginalTag("InMa_calcDistance", "" + distance);
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
//			w.addOriginalTag("InMa_Distance", "" + d);
			// get the Object with the lowest distance, or if equal, the smaller one.
			if(d<distance || (d==distance && w.getArea() < best.getArea())) {
				best = w;
				distance = d;
			}
		}
		
		//check if any thresholds are exceeded and shrink if necessary
		best = considerThresholds2(best, location);
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
	private Way considerThresholds2(Way best, Point location){
		
		Geometry guessed = best.getGeometry();
		
		double area = guessed.getArea();

		double radius = Double.MAX_VALUE;
		double d[];
		String combine;
		for(Entry<String,String> s : best.getTags().entrySet()) {
			combine = s.getKey().trim() + " - " + s.getValue().trim();
			// check for key + value
			d = thresholds.get(combine);
			if(d!=null) {
				if(d[0]<area && d[1]<radius)
					radius = d[1];
			} else {
				// and just key
				d = thresholds.get(s.getKey());
				if(d!=null) {
					if(d[0]<area && d[1]<radius)
						radius = d[1];
				}
			}
		}

		//if no threshold was exceeded, return the geometry unchanged
		if(radius == Double.MAX_VALUE)
			return best;
		//intersect with regular neighborhood
		Geometry neighborhood = Rules.createNgon(8, radius, location);
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
		
		if(n<3){
			return null;
		}
		
		if(radius == Double.POSITIVE_INFINITY || radius==Double.NEGATIVE_INFINITY){
			return null;
		}
		
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
		if (restrictions.size()==0 && v.size()==0)
			return 1.0f;
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
		String ret = restrictions.toString() + " -> " + weights.toString() + " -> {";
		for(Entry<String,double[]> e : thresholds.entrySet()) {
			ret += e.getKey() + "=" + e.getValue()[0] + "," + e.getValue()[1] + ", ";
		}
		return ret;
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
