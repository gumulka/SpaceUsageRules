package de.uni_hannover.spaceusagerules.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Polyline {
	
	List<Coordinate> points;
	double[] boundingBox = new double[4];
	
	public Polyline() {
		points = new LinkedList<Coordinate>();
		boundingBox[0] =  10000;
		boundingBox[1] = -10000;
		boundingBox[2] =  10000;
		boundingBox[3] = -10000;
	}
	
	public List<Coordinate> getPoints() {
		return points;
	}
	
	public Polyline(List<Coordinate> coords) {
		points = coords;
		boundingBox[0] =  10000;
		boundingBox[1] = -10000;
		boundingBox[2] =  10000;
		boundingBox[3] = -10000;
		for(Coordinate c : coords) {
			if(c.latitude < boundingBox[0])
				boundingBox[0] = c.latitude;
			if(c.latitude > boundingBox[1])
				boundingBox[1] = c.latitude;
			if(c.longitude < boundingBox[2])
				boundingBox[2] = c.longitude;
			if(c.longitude > boundingBox[3])
				boundingBox[3] = c.longitude;
		}
	}
	
	public void add(Coordinate c) {
		if(c.latitude < boundingBox[0])
			boundingBox[0] = c.latitude;
		if(c.latitude > boundingBox[1])
			boundingBox[1] = c.latitude;
		if(c.longitude < boundingBox[2])
			boundingBox[2] = c.longitude;
		if(c.longitude > boundingBox[3])
			boundingBox[3] = c.longitude;
		points.add(c);
	}

	
	public void addAll(Collection<Coordinate> coords) {
		for(Coordinate c : coords) {
		if(c.latitude < boundingBox[0])
			boundingBox[0] = c.latitude;
		if(c.latitude > boundingBox[1])
			boundingBox[1] = c.latitude;
		if(c.longitude < boundingBox[2])
			boundingBox[2] = c.longitude;
		if(c.longitude > boundingBox[3])
			boundingBox[3] = c.longitude;
		}
		points.addAll(coords);
	}
	
	/**
	 * checks if an intersection between two polygons is possible by using bounding boxes.
	 * 
	 * it is much faster, than calculation the acutual intersection. 
	 * @param a1 list of points for the first polygon
	 * @param a2 list of points for the second polygon
	 * @return true if the bounding boxes overlap.
	 */
	public boolean intersectionPossible(Polyline p2) {
		double[] bb = p2.getBoundingBox();
		if(boundingBox[0] < bb[0] && bb[0] < boundingBox[1]) {
			if(boundingBox[2] < bb[2] && bb[2] < boundingBox[3])
				return true;
			if(boundingBox[2] < bb[3] && bb[3] < boundingBox[3])
				return true;
		}
		if(boundingBox[0] < bb[1] && bb[1] < boundingBox[1]) {
			if(boundingBox[2] < bb[2] && bb[2] < boundingBox[3])
				return true;
			if(boundingBox[2] < bb[3] && bb[3] < boundingBox[3])
				return true;
		}
		return false;
	}
	
	public double[] getBoundingBox() {
		return boundingBox;
	}

	/**
	 * checks if a list of polygons forms an area.
	 * 
	 * @param l list of Coordinates
	 * @return true if they form an area.
	 */
	public boolean isArea() {
		// to be an Area it has to contain at least 3 different points.
		int different = 0;
		if(points.size()<3)
			return false;
		if(!points.get(0).equals(points.get(points.size()-1)))
			return false;
		for(int i = 0; i<points.size()-1; i++) {
			if(!points.get(i).equals(points.get(i+1)))
				different++;
			if(different>2)
				return true;
		}
		return false;
	}
	
	public double boundingBoxOverlapArea(Polyline p2) {
		double[] bb = p2.getBoundingBox();
		double x_min, x_max, y_min, y_max;
		if(boundingBox[0]<bb[0])
			x_min= bb[0];
		else
			x_min = boundingBox[0];
		if(boundingBox[1]<bb[1])
			x_max= boundingBox[1];
		else
			x_max = bb[1];
		if(boundingBox[2]<bb[2])
			y_min= bb[2];
		else
			y_min = boundingBox[2];
		if(boundingBox[3]<bb[3])
			y_max = boundingBox[3];
		else
			y_max= bb[3];
		if(x_min>x_max)
			return 0;
		if(y_min>y_max)
			return 0;
		return (x_max-x_min)*(y_max-y_min);
	}

	public double boundingBoxArea() {
		if(boundingBox[0]>boundingBox[1])
			return 0;
		if(boundingBox[2]>boundingBox[3])
			return 0;
		return (boundingBox[1] - boundingBox[0])*(boundingBox[3]-boundingBox[2]);
	}
	
	public Polyline overlapArea(Polyline p2) {
		// if one of them is not an Area, then there is no intersecting Area possible
		if(!isArea() || !p2.isArea())
			return null;
		// check if the an intersection is possible.
		// faster runtime, then calculation the actual intersection.
		if(!intersectionPossible(p2))
			return null;
		
		return null;
	}
	
	public static double calculateArea(List<Coordinate> area){
		return 0;
	}
}
