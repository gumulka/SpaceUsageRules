package de.uni_hannover.spaceusagerules.algorithm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class Rules {

	private Set<String> verbote;
	private Map<String,Float> weights;
	
	public Rules(Set<String> verbote, Map<String,Float> rules) {
		this.verbote = verbote;
		this.weights = rules;
	}

	public double calcDist(Coordinate c, Way w) {
		double distance = c.distanceTo(w.getPolyline());
		distance += 0.01;
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
	
	public Way calculateBest(List<Way> ways, Coordinate location) {
		Way best = null;
		double distance = Double.MAX_VALUE;
		double d;
		for(Way w : ways) {
			if(!w.isArea())
				continue;
			d = calcDist(location, w);
			w.addOriginalTag("InMa_Distance", "" + d);
			if(d<distance || (d==distance && w.getPolyline().boundingBoxArea() < best.getPolyline().boundingBoxArea())) {
				best = w;
				distance = d;
			}
		}
		return best;
	}
	
	public float overlap(Set<String> v) {
		if(verbote.size()==0 && v.size()==0)
			return 1;
		if(verbote.size()==0)
			return 0.5f;
		if(v.size()==0)
			return 0.5f;
		float start = 1, diff = .5f / verbote.size();
		for(String s : verbote) {
			if(!v.contains(s))
				start -= diff;
		}
		diff= .5f /v.size();
		for(String s : v) {
			if(!verbote.contains(s))
				start -= diff;
		}
		return start;
	}
	
}
