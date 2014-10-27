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
	/** distance from the origin to the line <BR>
	 * it's the d in ax+by = d */
	public double normalDistance;
	
	/** vector from start to end. Used as normal vector for an orthogonal line through start. */
	Coordinate lineVector;
	/** distance from the origin to the orthogonal line. */
	double inlineDistance;
	
	/** measures the length of this line section */
	double lineLength;
	
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
		//computes the scalar product: start*normal 
		normalDistance = orientedDistanceTo(start);
		
		lineVector = new Coordinate(end.latitude-start.latitude, end.longitude-start.longitude);
		lineLength = start.distanceTo(end);
		lineVector.longitude /= lineLength;
		lineVector.latitude /= lineLength;
		inlineDistance = start.longitude*lineVector.longitude + start.latitude*lineVector.latitude;

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
	 * Computes a change of basis with translation. The new origin is {@link #start}.
	 * Standard x is mapped to the vector from {@link #start} to {@link #end}.
	 * Standard y is mapped to {@link #normalVector}.
	 * @param p point to transformed.
	 * @return translated and transformed point.
	 */
	public Coordinate basisChange(Coordinate p){
		
		//new x is the distance to the line perpendicular to this line through start.
		double newX = orientedHNFDistance(p, lineVector, inlineDistance);
		
		//new y is the distance to the this line
		double newY = orientedHNFDistance(p, normalVector, normalDistance);
		
		//swap x and y since x is longitude and y is latitude
		return new Coordinate(newY, newX);
	}
	
	
	/**
	 * An arbitrary method to get the distance to a line by using HNF.
	 * @param c point to measure the distance to
	 * @param normal normal vector of the line, should be normalized
	 * @param d distance from the origin to the line
	 * @return oriented distance using HNF, may be negative
	 */
	private static double orientedHNFDistance(Coordinate c, Coordinate normal, double d){
		return c.longitude*normal.longitude + c.latitude*normal.latitude - d;
	}
	
	/**
	 * Computes the distance form this INFINITE line to a {@link Coordinate} using HNF.
	 * @param c point 
	 * @return oriented distance to the point. May be negative. The sign determines on which side of the line the point lies. 
	 */
	public double orientedDistanceTo(Coordinate c){
		return orientedHNFDistance(c, normalVector, normalDistance);
	}
	
	public boolean isOnLine(Coordinate p){
		
		Coordinate transformed = basisChange(p);
		//in the transformed state, p has to have latitude 0 
		//and longitude between 0 and 1 to be on this line.
		if(transformed.latitude != 0.) return false;
		if(transformed.longitude < 0.) return false;
		if(transformed.latitude > lineLength) return false;
		
		return true;
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
		else if(inline >= lineLength){
			return end.distanceTo(c);
		}
		
		//if c is "between" return the absolute value of the distance to the line.
		return Math.abs(orientedDistanceTo(c));
	}
	
	/**
	 * Checks if two line sections intersect.<BR>
	 * Tip: If you want to work with the intersection, if there is one, use 
	 * {@link #getIntersection(Line)} and {@link Coordinate#isNaN()} and avoid computing the
	 * same intersection twice.
	 * @param other the line to intersect with
	 * @return <code>true</code> if the two line sections meet - <code>false</code> otherwise
	 */
	public boolean isIntersecting(Line other){
		return !getIntersection(other).isNaN();
	}
	
	/**
	 * Computes the intersection of two line sections. Returns ({@link Double#NaN},{@link Double#NaN})
	 * if the lines are parallel or the sections simply don't meet.<BR>
	 * Tip: Use {@link Coordinate#isNaN()} to check if this is a valid intersection. That way 
	 * {@link #isIntersecting(Line)} doesn't have to compute the intersection a second time.
	 * @param other the line to intersect with.
	 * @return intersections of two line sections or (NaN,NaN) if there is no intersection
	 */
	public Coordinate getIntersection(Line other){
		Coordinate intersection = getCrossingPoint(other);
		if(intersection.isNaN()) return intersection;
		
		//check if other is part of both lines.
		if(isOnLine(intersection) && other.isOnLine(intersection)){
			return intersection;
		}
		else return new Coordinate(Double.NaN, Double.NaN);
	}
	
	/**
	 * Computes the intersection of two infinite lines. If they are parallel 
	 * ({@link Double#NaN},{@link Double#NaN}) is returned. Uses 
	 * <A HREF="https://en.wikipedia.org/wiki/Intersection_%28Euclidean_geometry%29#Two_lines">
	 * Cramers rule</A>.
	 * @param other the line to intersect with
	 * @return the intersection or (NaN,NaN) if parallel
	 */
	private Coordinate getCrossingPoint(Line other){
		
		double a1 = normalVector.longitude;
		double b1 = normalVector.latitude;
		double c1 = normalDistance;
		
		double a2 = other.getNormalVector().longitude;
		double b2 = other.getNormalVector().latitude;
		double c2 = other.normalDistance;
		
		double denom = a1*b2 - a2*b1;
		
		//if the denominator is 0 then the two lines are parallel or they are the same
		if(denom==0.){
			return new Coordinate(Double.NaN, Double.NaN);
		}
		
		double xs = (c1*b2 - c2*b1)/denom;
		double ys = (a1*c2 - a2*c1)/denom;
		
		return new Coordinate(xs,ys);
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
	
	public Coordinate getLineVector() {
		return lineVector;
	}

	@Override
	public String toString(){
		return start.toString() + "->" + end.toString();
	}
	
}
