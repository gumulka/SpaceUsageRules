package de.uni_hannover.spaceusagerules.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class Polyline  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4719490392798466108L;
	/**
	 * Eine Liste mit punkten, welche diese Polylinie ausmachen.
	 */
	List<Coordinate> points;
	/**
	 * die Bounding Box, welche sich um diese Polyline spannt.
	 */
	double[] boundingBox = new double[4];
	
	/**
	 * Konstruktor, welche die BoundingBox mit maximalen Werten Initialisiert, sodass sie bei dem erstbesten Punkt richtig ist.
	 */
	public Polyline() {
		points = new LinkedList<Coordinate>();
		boundingBox[0] =  Double.MAX_VALUE;
		boundingBox[1] =  Double.MIN_VALUE;
		boundingBox[2] =  Double.MAX_VALUE;
		boundingBox[3] =  Double.MIN_VALUE;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Coordinate> getPoints() {
		return points;
	}
	
	public Polyline(List<Coordinate> coords) {
		points = new LinkedList<Coordinate>();
		boundingBox[0] =  Double.MAX_VALUE;
		boundingBox[1] =  Double.MIN_VALUE;
		boundingBox[2] =  Double.MAX_VALUE;
		boundingBox[3] =  Double.MIN_VALUE;
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
			add(c);
		}
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
	
	public boolean insideBoundingBox(Coordinate c) {
		if(c.latitude<boundingBox[0] || c.latitude>boundingBox[1])
			return false;
		if(c.longitude<boundingBox[2] || c.longitude>boundingBox[3])
			return false;
		return true;
	}
	
	/**
	 * Determines it a point is in this polygon. Does not check if this object is 
	 * actually a polygon or just a line.<BR>
	 * The use of {@link #insideBoundingBox(Coordinate)} as a first approximation is encouraged,
	 * because it works much faster.<BR>
	 * Inspired by the <A HREF="https://en.wikipedia.org/wiki/Point_in_polygon#Ray_casting_algorithm">ray casting algorithm</A>
	 * @param p point to check
	 * @return <code>true</code> if the point is inside or on the outline - <code>false</code> otherwise.
	 */
	public boolean inside(Coordinate p){
		
		if(!insideBoundingBox(p))
			return false;
		
		List<Coordinate> zeroPoints = new LinkedList<Coordinate>();
		//check if p is one of the points
		for(Coordinate c : points){
			if(c==p || p.equals(c)) return true;
			zeroPoints.add(p.minus(c));
		}
		Coordinate zero = new Coordinate(0,0);
		
		//create lines and put them in a list
		List<Line> edges = new LinkedList<Line>();
		for(int i=0;i<zeroPoints.size()-1;i++){
			edges.add(new Line(zeroPoints.get(i), zeroPoints.get(i+1)));
		}
		
		//check if p is on any of the lines
		for(Line l : edges){
			if(l.isOnLine(zero)) return true;
		}
		
		//do ray casting
		//create the ray
		Line ray = new Line(zero, new Coordinate(0, 1.));
		//if necessary rotate ray until no corner of this polyline is on it
		while(Coordinate.pointsOnLine(zeroPoints, ray)){
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
	
	/**
	 * Checks if two polygons overlap, it is not checked if they are indeed polygons.
	 * It is tested if any of the corner points lies within the other polygon. For two
	 * polygons to overlap this has to be the case.<BR>
	 * The use of {@link #intersectionPossible(Polyline)} as a first approximation is encouraged.
	 * @param other the other polygon to overlap with
	 * @return <code>true</code> if they overlap - <code>false</code> otherwise
	 */
	public boolean isOverlapping(Polyline other){
		
		//check if they share a corner
		for(Coordinate c1 : points){
			for(Coordinate c2: other.points){
				if(c1.equals(c2)) return true;
			}
		}
		
		//check if a corner of this is in other
		for(Coordinate c1 : points){
			if(other.inside(c1)) return true;
		}
		
		//check if a corner of other is in this
		for(Coordinate c2 : other.points){
			if(inside(c2)) return true;
		}
		
		//if none of these hold true, then they don't overlap
		return false;
	}
	
	/**
	 * Computes the area of this polygon. If this is not a polygon 0 is returned.<BR>
	 * Uses the <A HREF="https://en.wikipedia.org/wiki/Shoelace_formula">
	 * shoelace algorithm</A>.
	 * @return the area of the polygon or 0. if it isn't a polygon
	 */
	public double area(){
		
		if(!isArea()) return 0.;
		
		double area=0.;
		Coordinate c1, c2;
		for(int i=0;i<points.size();i++){
			c1 = points.get(i);
			c2 = points.get((i+1)%points.size());
			area += (c1.longitude+c2.longitude)*(c2.latitude-c1.latitude);
		}
		area /= 2.;
		
		return area;
	}
	
	/**
	 * Returns a list with the intersections of two polylines (or outlines of polygons).
	 * If they don't meet, an empty list is returned.
	 * @param other the other polyline to intersect with
	 * @return list of intersections or empty list of there are no intersections.
	 */
	public List<Coordinate> getIntersections(Polyline other){
		
		List<Coordinate> intersections = new Vector<Coordinate>();

		for(Coordinate c1:points){
			for(Coordinate c2 : other.points){
				if(c1.equals(c2)) intersections.add(c1); 
			}
		}
		
		List<Line> edges1 = new Vector<Line>();
		for(int i=0;i<points.size()-1;i++){
			edges1.add(new Line(points.get(i),points.get(i+1)));
		}
		
		List<Line> edges2 = new Vector<Line>();
		for(int i=0;i<points.size()-1;i++){
			edges2.add(new Line(other.points.get(i), other.points.get(i+1)));
		}
		
		Coordinate cross;
		for(Line l1 : edges1){
			for(Line l2: edges2){
				cross = l1.getIntersection(l2);
				if(!cross.isNaN()) intersections.add(cross);
			}
		}
		
		return intersections;
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
	*/
	
}
