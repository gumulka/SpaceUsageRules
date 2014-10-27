package de.uni_hannover.spaceusagerules.test;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Vector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Polyline;

@RunWith(Parameterized.class)
public class PolylineTest {
	
	private Polyline square;
	private Coordinate position;
	private static double schrittweite = 0.01;
	
	public PolylineTest(double x, double y){
		position = new Coordinate(y,x);
		
		square = new Polyline();
		square.add(new Coordinate(1,1));
		square.add(new Coordinate(-1,1));
		square.add(new Coordinate(-1,-1));
		square.add(new Coordinate(1,-1));
		square.add(new Coordinate(1,1));
	}
	
	
	@Parameters
	public static Collection<Object[]> getTestParameters(){
		
		Collection<Object[]> output = new Vector<Object[]>();
		double x = 2.;
		double y = 2.;
		while(x>=-2.){
			while(y>=-2.){
				Object[] param = {x,y};
				output.add(param);
				y-=schrittweite;
			}
			x-=schrittweite;
		}
		
		
		return output;
	}
	
	
	
	@Test
	public void testInside() {
		
		boolean inX = position.longitude<=1. && position.longitude>=-1;
		boolean inY = position.latitude<=1. && position.latitude>=-1;
		boolean manuellInside = inX && inY;
		
		String message = position.toString()+" must ";
		if(!manuellInside) message += "not ";
		message += "be inside the square";
		
		assertTrue(message, manuellInside == square.inside(position));
		
	}

}
