package de.uni_hannover.spaceusagerules.test;

import java.util.LinkedList;
import java.util.List;

import de.uni_hannover.spaceusagerules.core.Coordinate;

public class InsideTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Coordinate> line = new LinkedList<Coordinate>();
		line.add(new Coordinate(52.3645,9.6868));
		line.add(new Coordinate(52.3648,9.6892));
		line.add(new Coordinate(52.3660,9.6888));
		line.add(new Coordinate(52.3657,9.6865));
		line.add(new Coordinate(52.3645,9.6868));
		Coordinate c = new Coordinate(52.3649,9.6873);
		System.out.println(c.inside(line));
		
		line = new LinkedList<Coordinate>();
		line.add(new Coordinate(52.38000,9.30500));
		line.add(new Coordinate(52.38010,9.30500));
		line.add(new Coordinate(52.38010,9.30510));
		line.add(new Coordinate(52.38000,9.30510));
		line.add(new Coordinate(52.38000,9.30500));
		c = new Coordinate(52.38005,9.305052);
		System.out.println(c.inside(line));
		
		line.add(new Coordinate(0.38000,0.30500));
		line.add(new Coordinate(0.38010,0.30500));
		line.add(new Coordinate(0.38010,0.30510));
		line.add(new Coordinate(0.38000,0.30510));
		line.add(new Coordinate(0.38000,0.30500));
		c = new Coordinate(0.38005,0.305052);
		System.out.println(c.inside(line));
		
		line = new LinkedList<Coordinate>();
		line.add(new Coordinate(5,9));
		line.add(new Coordinate(5,15));
		line.add(new Coordinate(50,15));
		line.add(new Coordinate(50,9));
		line.add(new Coordinate(5,9));
		c = new Coordinate(25,10);
		System.out.println(c.inside(line));

	}

}
