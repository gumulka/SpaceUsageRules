package de.uni_hannover.spaceusagerules.core;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import de.uni_hannover.spaceusagerules.core.Way;

public class Tag implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 505070413599238514L;

	private String tag;
	
	private List<Way> ways;
	
	public Tag(String tag) {
		this.tag = tag;
		ways = new LinkedList<Way>();
	}
	
	public String getName() {
		return tag;
	}
	
	public void addWay(Way w) {
		ways.add(w);
	}
	
	public boolean isEmpty() {
		return ways.isEmpty();
	}
	
	public List<Way> getWays() {
		return ways;
	}
	
	public String toString() {
		return tag;
	}
}
