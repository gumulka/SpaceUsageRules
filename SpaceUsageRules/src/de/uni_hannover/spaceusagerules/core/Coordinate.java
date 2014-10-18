package de.uni_hannover.spaceusagerules.core;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * This class represents a coordinate on the world map.
 *
 */
public class Coordinate  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 373886907184981572L;

	public double latitude;// like the y-Coordinate
	public double longitude; // like the x-Coordinate

	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/** 
	 * substracts another coordinate from this.
	 * @param c the other coordinate
	 * @return the difference between the two coordinates.
	 */
	public Coordinate minus(Coordinate c) {
		return new Coordinate(latitude - c.latitude, longitude-c.longitude);
	}

	/**
	 * Computes the quadratic distance from this point to another one.
	 * Pulling the square root is thereby left out. That is useful for comparing distances. 
	 * @param b the other point
	 * @return squared distance to the other point. Always greater than or equal to 0.
	 */
	public double squareDistanceTo(Coordinate b){
		//uses the formula (x2-x1)^2 + (y2-y1)^2
		return (b.latitude-latitude)*(b.latitude-latitude) + (b.longitude-longitude)*(b.longitude-longitude);
	}

	/**
	 * Computes the distance to another {@link Coordinate}.
	 * Uses {@link #squareDistanceTo(Coordinate)}.
	 * @param b the other coordinate
	 * @return distance to the other point. Always greater than or equal to 0.
	 */
	public double distanceTo(Coordinate b) {
		return Math.sqrt(squareDistanceTo(b));
	}

	/**
	 * Better use {@link Line#distanceTo(Coordinate)}.
	 * Computes distance to the line bounded by two points.
	 */
	@Deprecated
	public double distanceTo(Coordinate lineStart, Coordinate lineEnd){
		Line line = new Line(lineStart, lineEnd);
		return line.distanceTo(this);
	}

	/**
	 * Use {@link Polyline#inside(Coordinate)} instead.
	 * Determines if this point lies inside a given polygon.
	 * Inspired by the ray cast algorithm.
	 * @param polygon List of points that forms the polygon, first and last points have to be the same
	 * @return <code>true</code> if this lies inside - <code>false</code> otherwise
	 */
	@Deprecated
	public boolean inside(List<Coordinate> poly) {
		Polyline polygon = new Polyline(poly);
		return polygon.inside(this);
	}

	/**
	 * Checks if any point lies out of the given list lies on the given line.
	 * @param list of points to check
	 * @param line to check
	 * @return <code>true</code> if one or more points are on the line - <code>false</code> if no point is on the line
	 */
	static boolean pointsOnLine(List<Coordinate> list, Line line){
		for(Coordinate c : list){
			if(line.orientedDistanceTo(c)==0.) return true;
		}
		return false;
	}

	/**
	 * Computes the distance to a polygon. If the last point in the list doesn't equal the first
	 * point, it isn't treated as a closed polygon, but as a series of lines.
	 * @param polygon closed polygon or open polyline
	 * @return distance to a polygon, <code>0</code> if the point lies inside.
	 */
	@Deprecated
	public double distanceTo(List<Coordinate> polygon) {

		List<Line> edges = new Vector<Line>();
		for(int i=0;i<polygon.size()-1;i++){
			edges.add(new Line(polygon.get(i), polygon.get(i+1)));
		}

		if(polygon.get(0).equals(polygon.get(polygon.size()-1))){
			// wenn es ein polygon ist.
			//if the point is inside, then distance is 0
			if(inside(polygon)) return 0;
			//otherwise compute the distance to the polygon line
			else return distanceToNearestLine(edges);
		}
		else
		{
			// ansonsten ist es eine Linie mit mehreren Punkten.
			// -> get the distance to the nearest line.
			return distanceToNearestLine(edges);
		}
	}

	/**
	 * Computes the distance to a polygon. If the last point in the list doesn't equal the first
	 * point, it isn't treated as a closed polygon, but as a series of lines.
	 * @param polygon closed polygon or open polyline
	 * @return distance to a polygon, <code>0</code> if the point lies inside.
	 */
	public double distanceTo(Polyline polygon) {

		List<Coordinate> points = polygon.getPoints();
		
		List<Line> edges = new Vector<Line>();
		for(int i=0;i<points.size()-1;i++){
			edges.add(new Line(points.get(i), points.get(i+1)));
		}

		if(polygon.isArea()){
			// wenn es ein polygon ist.
			//if the point is inside, then distance is 0
			if(polygon.inside(this)) return 0;
			//otherwise compute the distance to the polygon line
			else return distanceToNearestLine(edges);
		}
		else
		{
			// ansonsten ist es eine Linie mit mehreren Punkten.
			// -> get the distance to the nearest line.
			return distanceToNearestLine(edges);
		}
	}

	/**
	 * Checks if one or both components are {@link Double#NaN}.
	 * @return <code>true</code> if one or both components are NaN - <code>false</code> otherwise
	 */
	public boolean isNaN(){
		if(longitude == Double.NaN) return true;
		if(latitude == Double.NaN) return true;
		return false;
	}


	/**
	 * Computes the distance to the nearest line. Can be used for both open and closed polygons. 
	 * @param edges list of lines
	 * @return distance to the nearest line, always >= 0.
	 */
	public double distanceToNearestLine(List<Line> edges){
		double nearest = edges.get(0).distanceTo(this);
		double testdist;
		for(int i=1;i<edges.size();i++){
			testdist = edges.get(i).distanceTo(this);
			if(testdist<nearest)
				nearest = testdist;
		}
		return nearest;
	}


	public boolean equals(Object o) {
		if(o instanceof Coordinate) {
			return ((Coordinate) o).latitude == latitude && ((Coordinate) o).longitude == longitude;
		}
		return false;
	}

	public String toString() {
		return String.format("%3.6f:%3.6f", latitude, longitude);
	}
}
