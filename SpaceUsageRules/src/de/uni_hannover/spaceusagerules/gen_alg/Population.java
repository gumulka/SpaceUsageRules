package de.uni_hannover.spaceusagerules.gen_alg;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.algorithm.Rules;
import de.uni_hannover.spaceusagerules.core.Way;

/**
 * @author Fabian Pflug
 *
 */
public class Population extends Rules implements Comparable<Population>{

	/** the maximum reachable fitness-value. the higher the better the results, to high an it might cause an overflow. */
	public static final int maxFitness = 10000;
	
	public static int GENERATOR = 8;
	
	/** the fitness value of this population	 */
	private int fitness = 0;
	
	public static long cal=0,inter=0;
	
	/**
	 * return the actual fitness value
	 * @return the fitness value
	 */
	public int getFitness() {
		return fitness;
	}
	
	public Population(Population p) {
		this.weights = p.weights;
		this.restrictions = p.restrictions;
		this.thresholds = p.thresholds;
	}
	
	
	/**
	 * Initializes a population with another population, by mutating the parameter.
	 * @param p another Population to mutate
	 * @param possible a list of all possible tags.
	 */
	public static Population mutate(Population p, Collection<String> possible) {
		Population ret = new Population(p);
		ret.weights = new TreeMap<String,Double>();
		ret.weights.putAll(p.weights);
		Random r = new Random();
		for(String line : possible) {
			if(r.nextInt(GENERATOR) == 0) {
				double d = r.nextDouble();
				d *=2;
				if(ret.weights.keySet().contains(line) && r.nextBoolean())
					ret.weights.remove(line);
				else
					ret.weights.put(line, d);
			}
		}
		return ret;
	}

	public static Population mutate2(Population p, Collection<String> possible) {
		Population ret = new Population(p);
		Random r = new Random();
		for(String line : possible) {
			if(r.nextInt(GENERATOR) == 0) {
				double d [] = new double[2];
				d[0] = r.nextDouble()/1024;
				d[1] = r.nextDouble()/1024;
				if(ret.thresholds.keySet().contains(line) && r.nextBoolean())
					ret.thresholds.remove(line);
				else
					ret.thresholds.put(line, d);
			}
		}
		return ret;
	}

	
	
	/**
	 * Initializes a population an puts random tags and values for the rules.
	 * @param possible a list of all possible tags.
	 */
	public Population(Collection<String> possible) {
		super();
		Random r = new Random();
		for(String p : possible) {
			if(r.nextInt(GENERATOR) == 0) {
				double d [] = new double[2];
				d[0] = r.nextDouble()/1024;
				d[1] = r.nextDouble()/1024;
				thresholds.put(p, d);
			}
		}
		for(String p : possible) {
			if(r.nextInt(GENERATOR) == 0) {
				double d = r.nextDouble();
				d *=2;
				weights.put(p, d);
			}
		}
	}
	
	/**
	 * Initializes a population an puts random tags and values for the rules.
	 * @param possible a list of all possible tags.
	 */
	public static Population generate2(Population q,Collection<String> possible) {
		Population ret = new Population(q);
		Random r = new Random();
		ret.thresholds = new TreeMap<String,double[]>();
		for(String p : possible) {
			if(r.nextInt(GENERATOR) == 0) {
				double d [] = new double[2];
				d[0] = r.nextDouble()/1024;
				d[1] = r.nextDouble()/1024;
				ret.thresholds.put(p, d);
			}
		}
		return ret;
	}
	
	public void addRestriction(String res) {
		if(restrictions==null)
			restrictions = new TreeSet<String>();
		restrictions.add(res);
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
	public static Population merge(Population p, Population q) {
		Random r = new Random();
		Population n = new Population();
		for(Entry<String,Double> e : q.weights.entrySet()) {
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
	 * combines this Population with another to form a new randomly merged population.
	 * @param p the other Population
	 * @return a new Population as randomly merged.
	 */
	public static Population merge2(Population p, Population q) {
		Random r = new Random();
		Population n = new Population();
		for(Entry<String,double[]> e : q.thresholds.entrySet()) {
			if(r.nextBoolean())
				n.thresholds.put(e.getKey(), e.getValue());
		}
		double x[] = new double[2];
		for(Entry<String,double[]> e : p.thresholds.entrySet()) {
			if(r.nextBoolean()) {
				x = n.thresholds.get(e.getKey());
				if(x!=null){
					double[] combine = e.getValue();
					combine[0] = (combine[0] + x[0])/2;
					combine[1] = (combine[1] + x[1])/2;
					e.setValue(combine);
				}
				n.thresholds.put(e.getKey(), e.getValue());
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
	public void calcFitness(List<Geometry> truths, List<Collection<Way>> possiblities, List<Point> locations) {
		Way best = null;
		fitness = 0;
		long a,b,c;
		for(int i = 0; i<truths.size(); i++) {
			Point l = locations.get(i);
			a = System.currentTimeMillis();
			best = calculateBest(possiblities.get(i),l);
			b = System.currentTimeMillis();
			double overlapArea = best.getGeometry().getEnvelope().intersection(truths.get(i).getEnvelope()).getArea();
			c = System.currentTimeMillis();
			overlapArea = Math.min(overlapArea/best.getArea(), overlapArea/truths.get(i).getArea());
			fitness += maxFitness * (overlapArea);
			over += (System.currentTimeMillis()-c);
			inter += (c-b);
			cal += (b-a);
		}
		fitness /= truths.size();
	}
	static long over = 0;
	
	
	/**
	 * Returns the number of rules used by this population
	 * @return number of rules
	 */
	public int getNumberOfRules() {
		return weights.size();
	}
	
	/**
	 * returns the number of thresholds defined for this population.
	 * @return number of thresholds
	 */
	public int getNumberOfThresholds() {
		return thresholds.size();
	}
	
	/**
	 * compares this population to another, by comparing their fitness values, and on equality their number of rules.
	 * a best population would have the highest fitness and the lowest number of rules.
	 */
	public int compareTo(Population o) {
		if(o.fitness==this.fitness) {
			if(this.weights.size()==o.weights.size())
				return this.thresholds.size()-o.thresholds.size();
			return this.weights.size()-o.weights.size();
		}
		return o.fitness-this.fitness;
	}

	/**
	 * makes a string representation of this population.
	 * \latexonly which is basically a line as described in \fref{sec:Eingabedaten_Wir} \endlatexonly
	 */
	public String toString() {
		return super.toString() + " " + fitness;
	}
}
