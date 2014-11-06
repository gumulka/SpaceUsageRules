package de.uni_hannover.spaceusagerules.gen_alg;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import de.uni_hannover.spaceusagerules.algorithm.Rules;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Polyline;
import de.uni_hannover.spaceusagerules.core.Way;

public class Population extends Rules implements Comparable<Population>{

	public static final int maxFitness = 10000;
	
	private int fitness;
	
	public int getFitness() {
		return fitness;
	}
	
	public Population(Population p, Collection<String> possible) throws IOException {
		weights = new TreeMap<String,Double>();
		weights.putAll(p.weights);
		Random r = new Random();
		for(String line : possible) {
			if(r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) {
				double d = r.nextDouble();
				d *=2;
				if(weights.keySet().contains(line) && r.nextBoolean())
					weights.remove(line);
				else
					weights.put(line, d);
			}
		}
	}
	
	public Population(Collection<String> possible) throws IOException {
		weights = new TreeMap<String,Double>();
		Random r = new Random();
		for(String p : possible) {
			if(r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) {
				double d = r.nextDouble();
				d *=2;
				weights.put(p, d);
			}
		}
	}
	
	private Population(boolean bla) {
		weights = new TreeMap<String,Double>();
	}
	
	public double getValue(String key) {
		if(weights.containsKey(key))
			return weights.get(key);
		else
			return 0;
	}
	
	public Population recombine(Population p) {
		Random r = new Random();
		Population n = new Population(false);
		for(Entry<String,Double> e : weights.entrySet()) {
			if(r.nextBoolean())
				n.addEntry(e);
		}
		double d;
		for(Entry<String,Double> e : p.weights.entrySet()) {
			if(r.nextBoolean()) {
				d = n.getValue(e.getKey());
				if(d!=0){
					e.setValue((e.getValue()+d)/2);
				}
				n.addEntry(e);
			}
		}
		return n;
	}
	
	private void addEntry(Entry<String, Double> e) {
		weights.put(e.getKey(), e.getValue());
	}
	
	public void calcFitness(List<Polyline> truths, List<Set<Way>> possiblities, List<Coordinate> locations) {
		Way best = null;
		fitness = 0;
		for(int i = 0; i<truths.size(); i++) {
			Coordinate l = locations.get(i);
			best = calculateBest(possiblities.get(i),l);
			double overlapArea = best.getPolyline().boundingBoxOverlapArea(truths.get(i));
			overlapArea = Math.min(overlapArea/best.getPolyline().boundingBoxArea(), overlapArea/truths.get(i).boundingBoxArea());
			fitness += maxFitness * (overlapArea);
		}
		fitness /= truths.size();
	}
	
	public int getNumberOfRules() {
		return weights.size();
	}
	
	public int compareTo(Population o) {
		if(o.fitness==this.fitness)
			return this.weights.size()-o.weights.size();
		return o.fitness-this.fitness;
	}

	public String toString() {
		String ret = "[";
		for(Entry<String,Double> e: weights.entrySet()) {
			if(ret.length()!=1)
				ret += ", ";
			ret += e.getKey() + " -> " + e.getValue();
		}
		ret += "] " + fitness;
		return ret;
	}
}
