package de.uni_hannover.spaceusagerules.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

//https://stackoverflow.com/questions/2667748/how-do-i-combine-complex-polygons
public class PolygonMerger {
	
	private Polyline A,B;
	private List<Coordinate> newA, newB;
	
	private List<Coordinate> intersections;
	
	private Map<Coordinate,Boolean> isInside;
	
	
	private PolygonMerger(Polyline A, Polyline B){
		this.A = A;
		this.B = B;
		
		isInside = new HashMap<Coordinate,Boolean>();
		
		newA = new Vector<Coordinate>();
		newB = new Vector<Coordinate>();
		intersections = new Vector<Coordinate>();
	}
	
	private void prepare(){
		
		//TODO compute intersections and insert them
		List<Coordinate> aPoints = A.getPoints();
		List<Coordinate> bPoints = new Vector<Coordinate>(B.getPoints());
		Line a,b;
		for(int i=0;i<aPoints.size()-1;i++){
			//add starting point of a line
			newA.add(aPoints.get(i));
			
			//check for intersections and add them to newA and insert them in newB
			for(int j=0;j<bPoints.size()-1;j++){
				
				a = new Line(aPoints.get(i), aPoints.get(i+1));
				b = new Line(bPoints.get(j), bPoints.get(j+1));
				
				//TODO ...
				
				
			}
			
			
			
		}
		
		
		
		
		//TODO run inside over all points that are no intersections
		
		//TODO test orientation and correct if necessary
	}
	
	
	private Polyline checkForSpecialCases(){
		//TODO stuff
		return null;
	}
	
	public static Polyline merge(Polyline A, Polyline B){
		
		//if one of them is not a polygon, stop right here
		if(!A.isArea() || !B.isArea())
			return null;
		
		PolygonMerger merger = new PolygonMerger(A,B);
		
		merger.prepare();
		
		
		
		return null;
	}
	
	private boolean isIntersection(Coordinate p){
		if(intersections.contains(p)) return true;
		//TODO is this loop really necessary? 
		for(Coordinate q : intersections){
			if(p.equals(q)) return true;
		}
		return false;
	}
	
	private boolean isInsideTheOther(Coordinate p){
		if(!isInside.containsKey(p)) return false;
		return isInside.get(p);
	}

	private List<Coordinate> getNewA() {
		return newA;
	}

	private List<Coordinate> getNewB() {
		return newB;
	}
	
}
