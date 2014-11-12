package de.uni_hannover.spaceusagerules.gen_alg;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import de.uni_hannover.spaceusagerules.algorithm.Rules;
import de.uni_hannover.spaceusagerules.core.CoordinateInMa;
import de.uni_hannover.spaceusagerules.core.Polyline;
import de.uni_hannover.spaceusagerules.core.Way;

/**
 * TODO javadoc schreiben.
 * @author Fabian Pflug
 *
 */
public class Population extends Rules implements Comparable<Population>{

	/** the maximum reachable fitness-value. the higher the better the results, to high an it might cause an overflow. */
	public static final int maxFitness = 10000;
	
	/** the fitness value of this population	 */
	private int fitness = 0;
	
	/**
	 * return the actual fitness value
	 * @return the fitness value
	 */
	public int getFitness() {
		return fitness;
	}
	
	/**
	 * Initializes a population with another population, by mutating the parameter.
	 * @param p another Population to mutate
	 * @param possible a list of all possible tags.
	 */
	public Population(Population p, Collection<String> possible) {
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
	
	/**
	 * Initializes a population an puts random tags and values for the rules.
	 * @param possible a list of all possible tags.
	 */
	public Population(Collection<String> possible) {
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

	/**
	 * Initializes an empty population with no rules.
	 */
	private Population() {
		weights = new TreeMap<String,Double>();
	}

	/**
	 * return the value for the given key in the entry set.
	 * @param key a possible key
	 * @return 0 if it does not exist, otherwise a value between 0 and 2.
	 */
	public double getValue(String key) {
		if(weights.containsKey(key))
			return weights.get(key);
		else
			return 0;
	}
	
	/**
	 * combines this Population with another to form a new randomly merged population.
	 * @param p the other Population
	 * @return a new Population as randomly merged.
	 */
	public Population recombine(Population p) {
		Random r = new Random();
		Population n = new Population();
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
	
	/**
	 * adds an Entry to the set of rules
	 * @param e
	 */
	private void addEntry(Entry<String, Double> e) {
		weights.put(e.getKey(), e.getValue());
	}
	
	/**
	 * calculates the fitness value, based on the given lists of examples
	 * each list should have the same length and every i-th entry in the list 
	 * should belong to the i-th entry in every other list. forming a list of datasets.
	 * @param truths a list of ground truth polygons
	 * @param possiblities a list of a collection of possible polygons
	 * @param locations a list of locations where to start from.
	 */
	public void calcFitness(List<Polyline> truths, List<Set<Way>> possiblities, List<CoordinateInMa> locations) {
		Way best = null;
		fitness = 0;
		for(int i = 0; i<truths.size(); i++) {
			CoordinateInMa l = locations.get(i);
			best = calculateBest(possiblities.get(i),l);
			double overlapArea = best.getPolyline().boundingBoxOverlapArea(truths.get(i));
			overlapArea = Math.min(overlapArea/best.getPolyline().boundingBoxArea(), overlapArea/truths.get(i).boundingBoxArea());
			fitness += maxFitness * (overlapArea);
		}
		fitness /= truths.size();
	}
	
	/**
	 * Returns the number of rules used by this population
	 * @return number of rules
	 */
	public int getNumberOfRules() {
		return weights.size();
	}
	
	/**
	 * compares this population to another, by comparing their fitness values, and on equality their number of rules.
	 * a best population would have the highest fitness and the lowest number of rules.
	 */
	public int compareTo(Population o) {
		if(o.fitness==this.fitness)
			return this.weights.size()-o.weights.size();
		return o.fitness-this.fitness;
	}

	/**
	 * makes a string representation of this population.
	 * \latexonly which is basically a line as described in \fref{sec:Eingabedaten_Wir} \endlatexonly
	 */
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
