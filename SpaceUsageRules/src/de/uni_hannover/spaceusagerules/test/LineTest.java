package de.uni_hannover.spaceusagerules.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Vector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Line;

@RunWith(Parameterized.class)
public class LineTest {
	
	private static Coordinate origin = new Coordinate(0.,0.);
	private static double radius=0.0001;
	private double phi=0.;
	
	private static double drehGeschwindigkeit = 0.01;
	private static double alpha = 0.001;
	private static double testradius = radius*0.7654;
	private static double tolerance = 1e-10;
	
	public LineTest(double phi){
		this.phi = phi;
	}
	
	@Parameters
	public static Collection<Object[]> createParameters(){
		
		Collection<Object[]> testParameters = new Vector<Object[]>();
		double phi = 0.;
		while(phi<2*Math.PI){
			Object[] testData = {phi};
			testParameters.add(testData);
			phi+=drehGeschwindigkeit;
		}
		
		return testParameters;
	}
	
	@Test
	public void testBasisChange() {
		
		Coordinate p1,p2,t1,t2,t3,t4;
		Line line;
		
		Coordinate transformed;
		double realDistance;

		p1 = new Coordinate(-radius*Math.sin(phi), -radius*Math.cos(phi));
		p2 = new Coordinate(radius*Math.sin(phi), radius*Math.cos(phi));
		line = new Line(p1,p2);
		
		//t1 liegt "kurz vor" der Strecke
		t1 = new Coordinate(testradius*Math.sin(phi+alpha), testradius*Math.cos(phi+alpha));
		//t2 liegt auf der Strecke
		t2 = new Coordinate(testradius*Math.sin(phi), testradius*Math.cos(phi));
		//t3 liegt "kurz hinter" der Strecke
		t3 = new Coordinate(testradius*Math.sin(phi-alpha), testradius*Math.cos(phi-alpha));
		//t4 liegt auf der Geraden aber außerhalb des Streckenabschnitts
		t4 = new Coordinate((radius+alpha)*Math.sin(phi), (alpha+radius)*Math.cos(phi));
		
		
		//Die Tests beginnen
		
		//die beiden Basisvectoren sollen Senkrecht aufeinander stehen, d.h. Skalarprodukt==0
		Coordinate normalVector = line.getNormalVector();
		Coordinate lineVector = line.getLineVector();
		double scalarproduct = normalVector.longitude*lineVector.longitude + normalVector.latitude*lineVector.latitude;
		assertEquals("scalarproduct of line basis must be 0.", 0., scalarproduct,0.);
		
		transformed = line.basisChange(t1);
		realDistance = Math.sin(alpha)*testradius;
		assertEquals("t1 must have distance "+realDistance, realDistance, transformed.latitude, tolerance);

		transformed = line.basisChange(t2);
		assertEquals("t2 must have distance 0.", 0., transformed.latitude, tolerance);
		
		transformed = line.basisChange(t3);
		realDistance = -Math.sin(alpha)*testradius;
		assertEquals("t3 must have distance "+realDistance, realDistance, transformed.latitude, tolerance);
		
		transformed = line.basisChange(t4);
		realDistance = radius*2+alpha;
		assertEquals("t4 must have distance "+realDistance+" from p1", realDistance, transformed.longitude, tolerance);
		assertEquals("t4 must have dinstance 0 from the line", 0., transformed.latitude, tolerance);
		
		//Der Ursprung soll auf der Geraden liegen
		transformed = line.basisChange(origin);
		realDistance = 0.;
		assertEquals("the oririn must have distance 0.", 0., transformed.latitude, tolerance);
	}
}
