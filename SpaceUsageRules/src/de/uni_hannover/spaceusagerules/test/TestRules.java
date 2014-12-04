package de.uni_hannover.spaceusagerules.test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.algorithm.Rules;

public class TestRules {

	@Test
	public void testOverlap() {
		Map<String,Double> trash = new TreeMap<String,Double>();
		
		List<String> empty = new LinkedList<String>();
		List<String> one = new LinkedList<String>();
		one.add("smoking=\"no\"");
		List<String> other = new LinkedList<String>();
		other.add("drinking=\"no\"");
		List<String> two = new LinkedList<String>();
		two.add("smoking=\"no\"");
		two.add("fishing=\"no\"");
		
		Rules emptyRules = new Rules();
		Rules twoRules = new Rules(two,trash,null);
		Rules oneRule = new Rules(one,trash,null);
		
		// Test for empty ruleSet
		assertEquals(emptyRules.overlap(empty),1.0f,0.0f);
		assertEquals(emptyRules.overlap(two),0.1f,0);
		
		// Test against another rule
		assertEquals(oneRule.overlap(other),0.0f,0);
		assertEquals(twoRules.overlap(other),0.0f,0);
		
		// Test against itself
		assertEquals(oneRule.overlap(one),1,0);
		assertEquals(twoRules.overlap(two),1,0);

		// Test if the position matters.
		assertEquals(oneRule.overlap(two),twoRules.overlap(one),0);
		
		assertEquals(oneRule.overlap(two),0.75,0); // because 100% of oneRule are covered by the other rules, but only 50% are covered otherwise.
		
	}
	
	@Test
	public void testNgon() {
		GeometryFactory gf = new GeometryFactory();
		Point p = gf.createPoint(new Coordinate());
		Rules.createNgon(0, Double.POSITIVE_INFINITY, p);
	}
	
}
