package de.uni_hannover.spaceusagerules.core;

import java.util.List;

public class Coordinate {
	
	public double latitude;
	public double longitude;
	
	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	
	public double distanceTo(Coordinate b) {
		//@TODO hier die Distanz berechnen.
		return 0;
	}
	
	public boolean inside(List<Coordinate> polygon) {
		//@TODO hier berechnen ob wir in dem polygon liegen.
		return false;
	}
	
	public double distanceTo(List<Coordinate> polygon) {
		//@TODO hier die Distanz berechnen.
		if(polygon.get(0).equals(polygon.get(polygon.size()-1))){
			// wenn es ein polygon ist. 
		}
		else
		{
			// ansonsten ist es eine Linie mit mehreren Punkten.
		}
		return 0;
	}
	
	public boolean equals(Object o) {
		if(o instanceof Coordinate) {
			return ((Coordinate) o).latitude == latitude && ((Coordinate) o).longitude == longitude;
		}
		return false;
	}
}
