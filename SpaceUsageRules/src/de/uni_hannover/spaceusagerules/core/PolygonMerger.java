package de.uni_hannover.spaceusagerules.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

//https://stackoverflow.com/questions/2667748/how-do-i-combine-complex-polygons
@Deprecated
public class PolygonMerger {
	
	private Polyline A,B;
	private List<CoordinateInMa> newA, newB;
	
	private List<CoordinateInMa> intersections;
	
	private Map<CoordinateInMa,Boolean> isInside;
	
	
	private PolygonMerger(Polyline A, Polyline B){
		this.A = A;
		this.B = B;
		
		isInside = new HashMap<CoordinateInMa,Boolean>();
		
		newA = new Vector<CoordinateInMa>();
		newB = new Vector<CoordinateInMa>();
		intersections = new Vector<CoordinateInMa>();
	}
	
	private void prepare(){
		
		//TODO compute intersections and insert them
		List<CoordinateInMa> aPoints = A.getPoints();
		List<CoordinateInMa> bPoints = new Vector<CoordinateInMa>(B.getPoints());
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
	
	private boolean isIntersection(CoordinateInMa p){
		if(intersections.contains(p)) return true;
		//TODO is this loop really necessary? 
		for(CoordinateInMa q : intersections){
			if(p.equals(q)) return true;
		}
		return false;
	}
	
	private boolean isInsideTheOther(CoordinateInMa p){
		if(!isInside.containsKey(p)) return false;
		return isInside.get(p);
	}

	private List<CoordinateInMa> getNewA() {
		return newA;
	}

	private List<CoordinateInMa> getNewB() {
		return newB;
	}
	
}
