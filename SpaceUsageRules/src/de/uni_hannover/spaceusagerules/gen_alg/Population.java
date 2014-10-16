package de.uni_hannover.spaceusagerules.gen_alg;

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

	public static final int maxFitness = 10000;
	
	private int fitness;
	private Map<String,Double> weights;
	
	public int getFitness() {
		return fitness;
	}
	
	public Population(Population p) throws IOException {
		weights = new TreeMap<String,Double>();
		weights.putAll(p.weights);
		Random r = new Random();
		File f = new File("possibilities.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line = br.readLine()) != null) {
			if(r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) {
				double d = r.nextDouble();
				d *=2;
				if(weights.keySet().contains(line) && r.nextBoolean())
					weights.remove(line);
				else
					weights.put(line, d);
			}
		}
		br.close();
	}
	
	public Population() throws IOException {
		weights = new TreeMap<String,Double>();
		Random r = new Random();
		File f = new File("possibilities.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line = br.readLine()) != null) {
			if(r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) {
				double d = r.nextDouble();
				d *=2;
				weights.put(line, d);
			}
		}
		br.close();
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
	
	public static double calcDist(Coordinate c, Way w, Map<String,Double> weights) {
		double distance = c.distanceTo(w.getCoordinates());
//		distance *= 1000;
		distance += 1;
		String combine;
		for(String s : w.getTags().keySet()) {
			combine = s + " - " + w.getValue(s);
			if(weights.keySet().contains(s)) {
				distance *= weights.get(s);
			}
			if(weights.keySet().contains(combine)) {
				distance *= weights.get(combine);
			}
		}
		return distance;
	}
	
	public static Way getNearestArea(Coordinate c, List<Way> ways, Map<String,Double> weights) {
		Way best = null;
		double distance = Double.MAX_VALUE;
		double d;
		for(Way w : ways) {
			if(!w.isArea())
				continue;
			d = calcDist(c, w, weights);
			if(d<distance || (d==distance && w.getPolyline().boundingBoxArea() < best.getPolyline().boundingBoxArea())) {
				best = w;
				distance = d;
			}
		}
		return best;
	}
	
	public void calcFitness(List<Polyline> truths, List<List<Way>> possiblities, List<Coordinate> locations) {
		Way best = null;
		fitness = 0;
		for(int i = 0; i<truths.size(); i++) {
			Coordinate l = locations.get(i);
			best = getNearestArea(l, possiblities.get(i), weights);
			double overlapArea = best.getPolyline().boundingBoxOverlapArea(truths.get(i));
			overlapArea = Math.min(overlapArea/best.getPolyline().boundingBoxArea(), overlapArea/truths.get(i).boundingBoxArea());
			fitness += maxFitness * (overlapArea);
		}
		fitness /= truths.size();
	}
	
	@Override
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
