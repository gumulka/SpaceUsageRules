package de.uni_hannover.spaceusagerules.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
/**
 * @todo Javadoc schreiben
 * @author Peter Zilz
 *
 */
@Deprecated
public class Polyline  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4719490392798466108L;
	/**
	 * Eine Liste mit punkten, welche diese Polylinie ausmachen.
	 */
	List<CoordinateInMa> points;
	/**
	 * die Bounding Box, welche sich um diese Polyline spannt.
	 */
	double[] boundingBox = new double[4];
	
	/**
	 * Konstruktor, welche die BoundingBox mit maximalen Werten Initialisiert, sodass sie bei dem erstbesten Punkt richtig ist.
	 */
	public Polyline() {
		points = new LinkedList<CoordinateInMa>();
		boundingBox[0] =  Double.MAX_VALUE;
		boundingBox[1] =  Double.MIN_VALUE;
		boundingBox[2] =  Double.MAX_VALUE;
		boundingBox[3] =  Double.MIN_VALUE;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<CoordinateInMa> getPoints() {
		return points;
	}
	
	public Polyline(List<CoordinateInMa> coords) {
		points = new LinkedList<CoordinateInMa>();
		boundingBox[0] =  Double.MAX_VALUE;
		boundingBox[1] =  Double.MIN_VALUE;
		boundingBox[2] =  Double.MAX_VALUE;
		boundingBox[3] =  Double.MIN_VALUE;
		for(CoordinateInMa c : coords) {
			add(c);
		}
	}
	
	public void add(CoordinateInMa c) {
		if(c.y < boundingBox[0])
			boundingBox[0] = c.y;
		if(c.y > boundingBox[1])
			boundingBox[1] = c.y;
		if(c.x < boundingBox[2])
			boundingBox[2] = c.x;
		if(c.x > boundingBox[3])
			boundingBox[3] = c.x;
		points.add(c);
	}

	
	public void addAll(Collection<CoordinateInMa> coords) {
		for(CoordinateInMa c : coords) {
			add(c);
		}
	}
	
	public List<CoordinateInMa> getBoundingBoxPolygon() {
		List<CoordinateInMa> back = new LinkedList<CoordinateInMa>();
		back.add(new CoordinateInMa(boundingBox[0],boundingBox[2]));
		back.add(new CoordinateInMa(boundingBox[1],boundingBox[2]));
		back.add(new CoordinateInMa(boundingBox[1],boundingBox[3]));
		back.add(new CoordinateInMa(boundingBox[0],boundingBox[3]));
		back.add(new CoordinateInMa(boundingBox[0],boundingBox[2]));
		return back;
	}
	
	/**
	 * checks if an intersection between two polygons is possible by using bounding boxes.
	 * 
	 * it is much faster, than calculation the actual intersection. 
	 * @param p2 the second polygon
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
	
	public boolean insideBoundingBox(CoordinateInMa c) {
		if(c.y<boundingBox[0] || c.y>boundingBox[1])
			return false;
		if(c.x<boundingBox[2] || c.x>boundingBox[3])
			return false;
		return true;
	}
	
	
	/**
	 * Determins it the point is inside this polygon or lies on the lines.
	 * Inspired by the <A HREF="https://en.wikipedia.org/wiki/Point_in_polygon#Ray_casting_algorithm">ray casting algorithm</A>
	 * @param p point to check
	 * @return <code>true</code> if the point is inside or on the outline - <code>false</code> otherwise.
	 */
	public boolean inside(CoordinateInMa p){
		
		// this is really fast and gives an approximation.
		if(!insideBoundingBox(p)){
			return false;
		}
		
		// move this polygon, so that the coordinate is the origin.
		List<CoordinateInMa> zeroPoints = new LinkedList<CoordinateInMa>();
		//check if p is one of the points
		for(CoordinateInMa corner : points){
			if(corner==p || p.equals(corner)){
				return true;
			}
			//make the point to prove the origin.
			zeroPoints.add(corner.minus(p));
		}
		// Counts the crosses with the positive range of the x-axis 
		int crosses = 0;
		
		for(int i=0;i<zeroPoints.size()-1;i++){
			CoordinateInMa c1,c2;
			int c1Q, c2Q;
			c1 = zeroPoints.get(i);
			c2 = zeroPoints.get(i+1);
			c1Q = c1.getQuadrant();
			c2Q = c2.getQuadrant();
			// sort by quadrant to have lesser ifs
			if(c2Q<c1Q) {
				CoordinateInMa tmp = c1;
				c1 = c2;
				c2 = tmp;
				c1Q = c1.getQuadrant();
				c2Q = c2.getQuadrant();
			}
			// first to fourth quadrant always crosses.
			if(c1Q==1 && c2Q==4)
				crosses++;
			// second to fourth only crosses if it also crosses the first quadrant
			if(c1Q==2 && c2Q==4) {
				double m = (c2.y - c1.y) / (c2.x - c1.x);
				double s = -m*c1.x + c1.y;
				if(s>0)
					crosses++;
				if(s==0) // the point lies on a line and is therefore inside.
					return true;
			}
			// first to third only crosses, if it also crosses the fourth quadrant.
			if(c1Q==1 && c2Q==3) {
				double m = (c1.y - c2.y) / (c1.x - c2.x);
				double s = -m*c2.x + c2.y;
				if(s<0)
					crosses++;
				if(s==0) // the point lies on a line and is therefore inside.
					return true;
			}
		}

		// we know at this point, that the coordinate is not on any line,
		// so if it is not an area, it is not inside the line.
		if(!isArea()) {
			return false;			
		}
		return (crosses%2==1);
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
		for(CoordinateInMa c1 : points){
			for(CoordinateInMa c2: other.points){
				if(c1.equals(c2)) return true;
			}
		}
		
		//check if a corner of this is in other
		for(CoordinateInMa c1 : points){
			if(other.inside(c1)) return true;
		}
		
		//check if a corner of other is in this
		for(CoordinateInMa c2 : other.points){
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
		CoordinateInMa c1, c2;
		for(int i=0;i<points.size();i++){
			c1 = points.get(i);
			c2 = points.get((i+1)%points.size());
			area += (c1.x+c2.x)*(c2.y-c1.y);
		}
		area /= 2.;
		
		return area;
	}
	
	
	/**
	 * TODO implementation
	 * return the union of this and the other polyline or null if they don't intersect/have no Point in common.
	 * @param other the second Polyline
	 * @return a new Polyline as the union of the two polylines or null
	 */
	public Polyline getUnion(Polyline other) {
		
		
		//if the bounding boxes are disjoint, then abort
		if(!intersectionPossible(other)) return null;
		
		return PolygonMerger.merge(this, other);
		
	}
	
	/**
	 * Returns a list with the intersections of two polylines (or outlines of polygons).
	 * If they don't meet, an empty list is returned.
	 * TODO methode erstellen, welche ein Polygon erstellt, dass aus der Schnittmenge zweier Polygone besteht.
	 * Die hier sieht gerade noch so falsch aus...
	 * @param other the other polyline to intersect with
	 * @return list of intersections or empty list of there are no intersections.
	 */
	public List<CoordinateInMa> getIntersectingPoints(Polyline other){
		List<CoordinateInMa> intersections = new Vector<CoordinateInMa>();

		for(CoordinateInMa c1:points){
			for(CoordinateInMa c2 : other.points){
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
		
		CoordinateInMa cross;
		for(Line l1 : edges1){
			for(Line l2: edges2){
				cross = l1.getIntersection(l2);
				if(!cross.isNaN()) intersections.add(cross);
			}
		}
		
		return intersections;
	}
	
	/**
	 * TODO implement + javadoc
	 * @param other
	 * @return
	 */
	public Polyline getIntersectionPolygon(Polyline other){
		
		//if one or both are not polygons, then abort
		if(!(isArea() && other.isArea())) return null;
		
		//if the bounding boxes are disjoint, then abort
		if(!intersectionPossible(other)) return null;
		
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(boundingBox);
		result = prime * result + ((points == null) ? 0 : points.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Polyline other = (Polyline) obj;
		if (!Arrays.equals(boundingBox, other.boundingBox))
			return false;
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points)) //TODO what if points are the same, but different order
			return false;
		return true;
	}
}
