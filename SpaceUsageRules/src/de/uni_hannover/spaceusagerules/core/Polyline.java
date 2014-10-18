package de.uni_hannover.spaceusagerules.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Polyline  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4719490392798466108L;
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
			add(c);
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
	
	public List<Coordinate> getBoundingBoxPolygon() {
		List<Coordinate> back = new LinkedList<Coordinate>();
		back.add(new Coordinate(boundingBox[0],boundingBox[2]));
		back.add(new Coordinate(boundingBox[1],boundingBox[2]));
		back.add(new Coordinate(boundingBox[1],boundingBox[3]));
		back.add(new Coordinate(boundingBox[0],boundingBox[3]));
		back.add(new Coordinate(boundingBox[0],boundingBox[2]));
		return back;
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
		x_min = Math.max(boundingBox[0],bb[0]);
		x_max = Math.min(boundingBox[1],bb[1]);
		y_min = Math.max(boundingBox[2],bb[2]);
		y_max = Math.min(boundingBox[3],bb[3]);
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
	
	/**
	 * Determines it a point is in this polygon. Does not check if this object is 
	 * actually a polygon or just a line.
	 * Inspired by the <A HREF="https://en.wikipedia.org/wiki/Point_in_polygon#Ray_casting_algorithm">ray casting algorithm</A>
	 * @param p point to check
	 * @return <code>true</code> if the point is inside or on the outline - <code>false</code> otherwise.
	 */
	public boolean inside(Coordinate p){
		
		//check if p is one of the points
		for(Coordinate c : points){
			if(c==p || p.equals(c)) return true;
		}
		
		//create lines and put them in a list
		List<Line> edges = new LinkedList<Line>();
		for(int i=0;i<points.size()-1;i++){
			edges.add(new Line(points.get(i), points.get(i+1)));
		}
		
		//check if p is on any of the lines
		for(Line l : edges){
			if(l.isOnLine(p)) return true;
		}
		
		//do ray casting
		//create the ray
		Line ray = new Line(p, new Coordinate(p.latitude, p.longitude+1.));
		//if necessary rotate ray until no corner of this polyline is on it
		while(Coordinate.pointsOnLine(points, ray)){
			Coordinate newEnd = ray.getEnd();
			newEnd.longitude += ray.getNormalVector().longitude/10.;
			newEnd.latitude += ray.getNormalVector().latitude/10.;
			ray.setEnd(newEnd);
		}
		
		//change basis of all edges
		List<Line> transformedEdges = new LinkedList<Line>();
		for(Line l: edges){
			transformedEdges.add(
					new Line(ray.basisChange(l.getStart()),
							ray.basisChange(l.getEnd())));
		}
		
		//delete all transformed edges where both points have longitude<0
		//and delete all lines with both ends on the same side of ray
		Coordinate start, end;
		for(int i=0;i<transformedEdges.size();i++){
			start = transformedEdges.get(i).getStart();
			end = transformedEdges.get(i).getEnd();
			
			if(start.longitude<0. && end.longitude<0.){
				transformedEdges.remove(i);
				i--;
				continue;
			}
			
			if(Math.signum(start.latitude) == Math.signum(end.latitude)){
				transformedEdges.remove(i);
				i--;
				continue;
			}
		}
		
		//the lines left are those that cross ray.
		//if their number is even p is outside the polygon
		if(transformedEdges.size()%2 == 0) return false;
		//if their number is odd p is inside the polygon
		else return true;
	}
	
	
	
	/*
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
	} // */
	
	
}
