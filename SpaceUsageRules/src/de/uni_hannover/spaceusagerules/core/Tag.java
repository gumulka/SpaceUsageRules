package de.uni_hannover.spaceusagerules.core;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public class Tag implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 505070413599238514L;

	private String tagname;
	private String tagid;
	
	private Set<Way> ways;
	
	public Tag(String tagname, String tagid) {
		this.tagname = tagname;
		this.tagid = tagid;
		ways = new TreeSet<Way>();
	}
	
	public String getName() {
		return tagname;
	}
	
	public String getTagId() {
		return tagid;
	}
	
	public void addWay(Way w) {
		ways.add(w);
	}
	
	public boolean isEmpty() {
		return ways.isEmpty();
	}
	
	public Set<Way> getWays() {
		return ways;
	}
	
	public String toString() {
		return tagname;
	}
}
