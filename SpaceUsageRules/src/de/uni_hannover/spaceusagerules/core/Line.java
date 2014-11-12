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
@Deprecated
public class Line {
	
	/** starting point of the line section */
	private CoordinateInMa start;
	/** ending point of the line section */
	private CoordinateInMa end;
	
	/** normal vector of the line */
	private CoordinateInMa normalVector;
	/** distance from the origin to the line <BR>
	 * it's the d in ax+by = d */
	public double normalDistance;
	
	/** vector from start to end (= directional vector of the line). 
	 * Used as normal vector for an orthogonal line through start. */
	CoordinateInMa lineVector;
	/** distance from the origin to the orthogonal line. */
	double inlineDistance;
	
	/** measures the length of this line section */
	double lineLength;
	
	/**
	 * Creates a line ans its normal vector.
	 */
	public Line(CoordinateInMa start, CoordinateInMa end){
		this.start = start;
		this.end = end;
		
		updateValues();
	}
	
	/**
	 * Computes {@link #normalVector}, {@link #normalDistance}, {@link #lineVector} and 
	 * {@link #inlineDistance} from {@link #start} and {@link #end};
	 */
	private void updateValues(){
		normalVector = computeNormalVector(start, end);
		normalDistance = 0.;
		//computes the scalar product: start*normal 
		normalDistance = orientedDistanceTo(start);
		
		lineVector = new CoordinateInMa(end.y-start.y, end.x-start.x);
		lineLength = start.distanceTo(end);
		lineVector.x /= lineLength;
		lineVector.y /= lineLength;
		inlineDistance = start.x*lineVector.x + start.y*lineVector.y;

	}
	
	/**
	 * Computes the normal vector to the line given by the two points.
	 * @param start first point of the line
	 * @param end second point of the line, has to have different Coordinates
	 * @return normal vector of the line with length 1.
	 */
	private static CoordinateInMa computeNormalVector(CoordinateInMa start, CoordinateInMa end){
		//create vector from start to end.
		CoordinateInMa normalVector = new CoordinateInMa(end.y-start.y, end.x-start.x);
		
		//rotate 90°: (-y,x)
		double newX = -normalVector.y;
		double newY = normalVector.x;
		
		//scale to length 1
		double length = newX*newX + newY*newY;
		length = Math.sqrt(length);
		newX /= length;
		newY /= length;
		
		normalVector.x = newX;
		normalVector.y = newY;
		
		return normalVector;
	}
	
	/**
	 * Computes a change of basis with translation. The new origin is {@link #start}.
	 * Standard x is mapped to the vector from {@link #start} to {@link #end}.
	 * Standard y is mapped to {@link #normalVector}.
	 * @param p point to transformed.
	 * @return translated and transformed point.
	 */
	public CoordinateInMa basisChange(CoordinateInMa p){
		
		//new x is the distance to the line perpendicular to this line through start.
		double newX = orientedHNFDistance(p, lineVector, inlineDistance);
		
		//new y is the distance to the this line
		double newY = orientedHNFDistance(p, normalVector, normalDistance);
		
		//swap x and y since x is longitude and y is latitude
		return new CoordinateInMa(newY, newX);
	}
	
	
	/**
	 * An arbitrary method to get the distance to a line by using HNF.
	 * @param c point to measure the distance to
	 * @param normal normal vector of the line, should be normalized
	 * @param d distance from the origin to the line
	 * @return oriented distance using HNF, may be negative
	 */
	private static double orientedHNFDistance(CoordinateInMa c, CoordinateInMa normal, double d){
		return c.x*normal.x + c.y*normal.y - d;
	}
	
	/**
	 * Computes the distance form this INFINITE line to a {@link CoordinateInMa} using HNF.
	 * @param c point 
	 * @return oriented distance to the point. May be negative. The sign determines on which side of the line the point lies. 
	 */
	public double orientedDistanceTo(CoordinateInMa c){
		return orientedHNFDistance(c, normalVector, normalDistance);
	}
	
	/**
	 * Tests if a point is on this line section. p has to be between {@link #start} 
	 * and {@link #end}. If p is on the line but outside of these two points, 
	 * <code>false</code> is returned.
	 * @param p point to be tested
	 * @return <code>true</code> if p lies on the line section, <code>false</code> otherwise 
	 */
	public boolean isOnLine(CoordinateInMa p){
		
		CoordinateInMa transformed = basisChange(p);
		//in the transformed state, p has to have latitude 0 
		//and longitude between 0 and the length of the line to be on this line.
		if(transformed.y != 0.) return false;
		if(transformed.x < 0.) return false;
		if(transformed.x > lineLength) return false;
		
		return true;
	}
	
	/**
	 * Computes the distance form a given {@link CoordinateInMa} to this line section bounded by the start 
	 * and end points. Hence it is likely, that the distance to one of the boundary points is given,
	 * using {@link CoordinateInMa#distanceTo(CoordinateInMa)}.
	 * @param c point to measure the distance to
	 * @return distance from this line section to the point. Always >= 0.
	 */
	public double distanceTo(CoordinateInMa c){
		
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
	 * {@link #getIntersection(Line)} and {@link CoordinateInMa#isNaN()} and avoid computing the
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
	 * Tip: Use {@link CoordinateInMa#isNaN()} to check if this is a valid intersection. That way 
	 * {@link #isIntersecting(Line)} doesn't have to compute the intersection a second time.
	 * @param other the line to intersect with.
	 * @return intersections of two line sections or (NaN,NaN) if there is no intersection
	 */
	public CoordinateInMa getIntersection(Line other){
		CoordinateInMa intersection = getCrossingPoint(other);
		if(intersection.isNaN()) return intersection;
		
		//check if other is part of both lines.
		if(isOnLine(intersection) && other.isOnLine(intersection)){
			return intersection;
		}
		else return new CoordinateInMa(Double.NaN, Double.NaN);
	}
	
	/**
	 * Computes the intersection of two infinite lines. If they are parallel 
	 * ({@link Double#NaN},{@link Double#NaN}) is returned. Uses 
	 * <A HREF="https://en.wikipedia.org/wiki/Intersection_%28Euclidean_geometry%29#Two_lines">
	 * Cramers rule</A>.
	 * @param other the line to intersect with
	 * @return the intersection or (NaN,NaN) if parallel
	 */
	private CoordinateInMa getCrossingPoint(Line other){
		
		double a1 = normalVector.x;
		double b1 = normalVector.y;
		double c1 = normalDistance;
		
		double a2 = other.getNormalVector().x;
		double b2 = other.getNormalVector().y;
		double c2 = other.normalDistance;
		
		double denom = a1*b2 - a2*b1;
		
		//if the denominator is 0 then the two lines are parallel or they are the same
		if(denom==0.){
			return new CoordinateInMa(Double.NaN, Double.NaN);
		}
		
		double xs = (c1*b2 - c2*b1)/denom;
		double ys = (a1*c2 - a2*c1)/denom;
		
		return new CoordinateInMa(xs,ys);
	}
	
	
	/** Returns the start point of the line section */
	public CoordinateInMa getStart() {
		return start;
	}
	
	/**
	 * Sets a new start point and updates the normal vector.
	 * @param start new start point
	 */
	public void setStart(CoordinateInMa start) {
		this.start = start;
		updateValues();
	}
	
	/** returns the end point */
	public CoordinateInMa getEnd() {
		return end;
	}
	
	/**
	 * Sets a new end point and updates the normal vector.
	 * @param end new end point
	 */
	public void setEnd(CoordinateInMa end) {
		this.end = end;
		updateValues();
	}

	/** returns the normal vector */
	public CoordinateInMa getNormalVector() {
		return normalVector;
	}
	
	/**
	 * Returns the vector from {@link #start} to {@link #end}.
	 * @return direction vector
	 */
	public CoordinateInMa getLineVector() {
		return lineVector;
	}
	
	/**
	 * Represents this line by printing {@link #start} and {@link #end} as Strings.
	 */
	@Override
	public String toString(){
		return start.toString() + "->" + end.toString();
	}
	
}
