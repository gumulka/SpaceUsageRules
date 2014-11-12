package de.uni_hannover.spaceusagerules.core;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This class represents a coordinate on the world map.
 *
 */
public class CoordinateInMa extends com.vividsolutions.jts.geom.Coordinate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 373886907184981572L;

//	public double y;// like the y-Coordinate
//	public double x; // like the x-Coordinate
	
	/**
	 * 
	 * @param latitude  y-coordinate
	 * @param longitude x-coordinate
	 */
	public CoordinateInMa(double latitude, double longitude) {
		super(longitude,latitude);
	}
	
	public CoordinateInMa(Coordinate c){
		super(c.x,c.y);
	}
	
	
	/** 
	 * substracts another coordinate from this.
	 * @param c the other coordinate
	 * @return the difference between the two coordinates.
	 */
	public CoordinateInMa minus(CoordinateInMa c) {
		return new CoordinateInMa(y - c.y, x-c.x);
	}

	/**
	 * Computes the quadratic distance from this point to another one.
	 * Pulling the square root is thereby left out. That is useful for comparing distances. 
	 * @param b the other point
	 * @return squared distance to the other point. Always greater than or equal to 0.
	 */
	public double squareDistanceTo(CoordinateInMa b){
		//uses the formula (x2-x1)^2 + (y2-y1)^2
		return (b.y-y)*(b.y-y) + (b.x-x)*(b.x-x);
	}

	/**
	 * Computes the distance to another {@link CoordinateInMa}.
	 * Uses {@link #squareDistanceTo(CoordinateInMa)}.
	 * @param b the other coordinate
	 * @return distance to the other point. Always greater than or equal to 0.
	 */
	public double distanceTo(CoordinateInMa b) {
		return Math.sqrt(squareDistanceTo(b));
	}

	/**
	 * Checks if one or both components are {@link Double#NaN}.
	 * @return <code>true</code> if one or both components are NaN - <code>false</code> otherwise
	 */
	public boolean isNaN(){
		if(x == Double.NaN) return true;
		if(y == Double.NaN) return true;
		return false;
	}

	
	public int getQuadrant() {
		if(y<0 && x<0)
			return 3;
		if(y<0)
			return 4;
		if(x<0)
			return 2;
		return 1;
	}
	
	
	public double getLongitude(){
		return x;
	}
	public void setLongitude(double lon){
		x = lon;
	}
	
	public double getLatitude(){
		return y;
	}
	public void setLatitude(double lat){
		y = lat;
	}
	
	

	public boolean equals(Object o) {
		if(o instanceof CoordinateInMa) {
			return ((CoordinateInMa) o).y == y && ((CoordinateInMa) o).x == x;
		}
		return false;
	}

	public String toString() {
		return String.format("%3.6f:%3.6f", y, x);
	}
}
