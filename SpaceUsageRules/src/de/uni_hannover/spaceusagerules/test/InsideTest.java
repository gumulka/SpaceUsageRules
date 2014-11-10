package de.uni_hannover.spaceusagerules.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Polyline;

public class InsideTest {

	@Test
	public void testInside() {
		Polyline line = new Polyline(); // einfaches Polygon, welches etwas gedreht ist.
		line.add(new Coordinate(52.3645,9.6868));
		line.add(new Coordinate(52.3648,9.6892));
		line.add(new Coordinate(52.3660,9.6888));
		line.add(new Coordinate(52.3657,9.6865));
		line.add(new Coordinate(52.3645,9.6868));
		Coordinate c = new Coordinate(52.3649,9.6873);
		assertTrue(line.inside(c));
		
		line = new Polyline(); // einfaches, aber sehr kleines Polygon.
		line.add(new Coordinate(52.38000,9.30500));
		line.add(new Coordinate(52.38010,9.30500));
		line.add(new Coordinate(52.38010,9.30510));
		line.add(new Coordinate(52.38000,9.30510));
		line.add(new Coordinate(52.38000,9.30500));
		c = new Coordinate(52.38005,9.305052);
		assertTrue(line.inside(c));

		line = new Polyline(); // Punkt nur sehr minimal daneben.
		line.add(new Coordinate(52.38000,9.30500));
		line.add(new Coordinate(52.38010,9.30500));
		line.add(new Coordinate(52.38010,9.30510));
		line.add(new Coordinate(52.38000,9.30510));
		line.add(new Coordinate(52.38000,9.30480));
		c = new Coordinate(52.380010,9.30499);
		assertTrue(!line.inside(c)); 

		
		line = new Polyline(); // einfaches, aber sehr kleines Polygon. nahe am Nullpunkt.
		line.add(new Coordinate(0.38000,0.30500));
		line.add(new Coordinate(0.38010,0.30500));
		line.add(new Coordinate(0.38010,0.30510));
		line.add(new Coordinate(0.38000,0.30510));
		line.add(new Coordinate(0.38000,0.30500));
		c = new Coordinate(0.38005,0.305052);
		assertTrue(line.inside(c));
		
		line = new Polyline(); // sehr großes Polygon. besonders einfach zu erreichen.
		line.add(new Coordinate(5,9));
		line.add(new Coordinate(5,15));
		line.add(new Coordinate(50,15));
		line.add(new Coordinate(50,9));
		line.add(new Coordinate(5,9));
		c = new Coordinate(25,10);
		assertTrue(line.inside(c));
		
		line = new Polyline(); // Eine Linie und der Test, ob der Punkt auf der Linie liegt.
		line.add(new Coordinate(5,9));
		line.add(new Coordinate(5,15));
		line.add(new Coordinate(50,15));
		c = new Coordinate(25,10);
		assertTrue(!line.inside(c));
		 
		line = new Polyline(); // konvexes Polygon
		line.add(new Coordinate(52.42934,9.72744));
		line.add(new Coordinate(52.42951,9.72740)); // it looks like this. with bottom left as first, going clockwise
		line.add(new Coordinate(52.42951,9.72751)); //   __  __
		line.add(new Coordinate(52.42945,9.72753)); //   | |_| |
		line.add(new Coordinate(52.42945,9.72758)); //   |_____|
		line.add(new Coordinate(52.42952,9.72756)); 
		line.add(new Coordinate(52.42952,9.72767));
		line.add(new Coordinate(52.42935,9.72770));
		line.add(new Coordinate(52.42934,9.72744));
		// Punkt in der Aushölung
		c = new Coordinate(52.42951,9.72754);
		assertTrue(!line.inside(c));
		// Punkt im Flügel.
		c = new Coordinate(52.42950,9.72761);
		assertTrue(line.inside(c));
		
	}
	

}
