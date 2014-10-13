package de.uni_hannover.spaceusagerules.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Polyline;
import de.uni_hannover.spaceusagerules.core.Way;

public class Population implements Comparable<Population>{

	private int fitness;
	private Map<String,Double> weights;
	
	public Population() throws IOException {
		weights = new TreeMap<String,Double>();
		Random r = new Random();
		File f = new File("possibilities.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line = br.readLine()) != null) {
			if(r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) {
				weights.put(line, r.nextDouble());
			}
		}
		br.close();
	}
	
	private Population(boolean bla) {
		weights = new TreeMap<String,Double>();
	}
	
	public Population recombine(Population p) {
		Random r = new Random();
		Population n = new Population(false);
		for(Entry<String,Double> e : weights.entrySet()) {
			if(r.nextBoolean() && r.nextBoolean())
				n.addEntry(e);
		}
		for(Entry<String,Double> e : p.weights.entrySet()) {
			if(r.nextBoolean() && r.nextBoolean())
				n.addEntry(e);
		}
		return n;
	}
	
	private void addEntry(Entry<String, Double> e) {
		weights.put(e.getKey(), e.getValue());
	}
	
	private double calcDist(Coordinate c, Way w) {
		double distance = c.distanceTo(w.getCoordinates());
		String combine;
		for(String s : w.getTags().keySet()) {
			combine = s + " - " + w.getValue(s);
			if(weights.keySet().contains(s)) {
				distance += weights.get(s);
			}
			if(weights.keySet().contains(combine)) {
				distance += weights.get(combine);
			}
		}
		return distance;
	}
	
	public void calcFitness(List<Polyline> truths, List<List<Way>> possiblities, List<Coordinate> locations) {
		Way best = null;
		double distance = 0;
		double d;
		fitness = 0;
		for(int i = 0; i<truths.size(); i++) {
			Coordinate l = locations.get(i);
			for(Way w : possiblities.get(i)) {
				if(best==null) {
					best = w;
					distance = calcDist(l,w);
					continue;
				}
				d = calcDist(l, w);
				if(d<distance) {
					best = w;
					distance = d;
				}
			}
			double overlapArea = best.getPolyline().boundingBoxOverlapArea(truths.get(i));
			overlapArea = Math.min(overlapArea/best.getPolyline().boundingBoxArea(), overlapArea/truths.get(i).boundingBoxArea());
			fitness += 10000000 * (overlapArea);
		}
		
		fitness /= (weights.size()+1);
	}
	
	@Override
	public int compareTo(Population o) {
		return o.fitness-this.fitness;
	}

	public String toString() {
		String ret = "[" + fitness;
		for(Entry<String,Double> e: weights.entrySet()) {
			ret += ", " + e.getKey() + " -> " + e.getValue();
		}
		ret += "]";
		return ret;
	}
}
