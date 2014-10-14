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

	private static final int lengthInfluence = 300; // high -> low influence
	public static final int maxFitness = 10000;
	
	private int fitness;
	private Map<String,Double> weights;
	
	public int getFitness() {
		return fitness;
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
					distance = calcDist(l,w, weights);
					continue;
				}
				d = calcDist(l, w, weights);
				if(d<distance) {
					best = w;
					distance = d;
				}
			}
			double overlapArea = best.getPolyline().boundingBoxOverlapArea(truths.get(i));
			overlapArea = Math.min(overlapArea/best.getPolyline().boundingBoxArea(), overlapArea/truths.get(i).boundingBoxArea());
			fitness += maxFitness * (overlapArea);
		}
		double bla = (weights.size()+ lengthInfluence);
		bla = lengthInfluence/bla;
		fitness = (int) (fitness * bla);
		fitness /= truths.size();
	}
	
	@Override
	public int compareTo(Population o) {
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
