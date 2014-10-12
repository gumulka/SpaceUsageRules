package de.uni_hannover.spaceusagerules.core;

/**
 * Represents the line between two points.
 * The line is represented in two ways. First by a start and an end point. 
 * Second by the <a Href="https://en.wikipedia.org/wiki/Hesse_normal_form">Hesse normal form</a>.
 * The HNF simplifies computing the distance to a point enormously. 
 * Which is the main reason for this class.  
 * @author Peter Zilz
 *
 */
public class Line {
	
	/** starting point of the line section */
	private Coordinate start;
	/** ending point of the line section */
	private Coordinate end;
	
	/** normal vector of the line */
	private Coordinate normalVector;
	/** distance from the origin to the line */
	private double normalDistance;
	
	/** vector from start to end. Used as normal vector for an orthogonal line through start. */
	private Coordinate lineVector;
	/** distance from the origin to the orthogonal line. */
	private double inlineDistance; 
	
	/**
	 * Creates a line with normal vector.
	 */
	public Line(Coordinate start, Coordinate end){
		this.start = start;
		this.end = end;
		
		updateValues();
	}
	
	/**
	 * Computes {@link #normalVector}, {@link #normalDistance}, {@link #lineVector} and 
	 * {@link #inlineDistance} out of {@link #start} and {@link #end};
	 */
	private void updateValues(){
		normalVector = computeNormalVector(start, end);
		normalDistance = 0.;
		normalDistance = orientedDistanceTo(start);
		
		lineVector = new Coordinate(end.latitude-start.latitude, end.longitude-start.longitude);
		inlineDistance = lineVector.longitude*start.longitude + lineVector.latitude*start.longitude;
	}
	
	
	/**
	 * Computes the normal vector to the line given by the two points.
	 * @param start first point of the line
	 * @param end second point of the line, has to have different Coordinates
	 * @return normal vector of the line with length 1.
	 */
	private static Coordinate computeNormalVector(Coordinate start, Coordinate end){
		//create vector from start to end.
		Coordinate normalVector = new Coordinate(end.latitude-start.latitude, end.longitude-start.longitude);
		
		//rotate 90°: (-y,x)
		double newX = -normalVector.latitude;
		double newY = normalVector.longitude;
		
		//scale to length 1
		double length = newX*newX + newY*newY;
		length = Math.sqrt(length);
		newX /= length;
		newY /= length;
		
		normalVector.longitude = newX;
		normalVector.latitude = newY;
		
		return normalVector;
	}
	
	/**
	 * An arbitrary method to get the distance to a line by using HNF.
	 * @param c point to measure the distance to
	 * @param normal normal vector of the line, should be normalized
	 * @param d distance from the origin to the line
	 * @return oriented distance using HNF, may be negative
	 */
	private static double orientedHNFDistance(Coordinate c, Coordinate normal, double d){
		return c.longitude*normal.longitude + c.latitude*c.longitude - d;
	}
	
	/**
	 * Computes the distance form this INFINITE line to a {@link Coordinate} using HNF.
	 * @param c point 
	 * @return oriented distance to the point. May be negative. The sign determines on which side of the line the point lies. 
	 */
	public double orientedDistanceTo(Coordinate c){
		return orientedHNFDistance(c, normalVector, normalDistance);
	}
	
	/**
	 * Computes the distance form a given {@link Coordinate} to this line section bounded by the start 
	 * and end points. Hence it is likely, that the distance to one of the boundary points is given,
	 * using {@link Coordinate#distanceTo(Coordinate)}.
	 * @param c point to measure the distance to
	 * @return distance from this line section to the point. Always >= 0.
	 */
	public double distanceTo(Coordinate c){
		
		//test if c it "between" the two bounding points.
		double inline = orientedHNFDistance(c, lineVector, inlineDistance);
		//if c is "outside" the line section return the distance to the matching boundary point.
		if(inline <= 0.){
			return start.distanceTo(c);
		}
		else if(inline >= 1.){
			return end.distanceTo(c);
		}
		
		//if c is "between" return the absolute value of the distance to the line.
		return Math.abs(orientedDistanceTo(c));
	}
	
	/** Returns the start point of the line section */
	public Coordinate getStart() {
		return start;
	}
	
	/**
	 * Sets a new start point and updates the normal vector.
	 * @param start new start point
	 */
	public void setStart(Coordinate start) {
		this.start = start;
		updateValues();
	}
	
	/** returns the end point */
	public Coordinate getEnd() {
		return end;
	}
	
	/**
	 * Sets a new end point and updates the normal vector.
	 * @param end new end point
	 */
	public void setEnd(Coordinate end) {
		this.end = end;
		updateValues();
	}

	/** returns the normal vector */
	public Coordinate getNormalVector() {
		return normalVector;
	}
	
}
