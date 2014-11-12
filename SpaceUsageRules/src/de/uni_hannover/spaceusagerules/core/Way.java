package de.uni_hannover.spaceusagerules.core;


import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Representation of an OSM-Object
 * 
 * @todo die Javadoc ins englische umschreiben.
 * @author Fabian Pflug
 */
public class Way implements Serializable, Comparable<Way>{

    /**	 */
	private static final long serialVersionUID = 4356641146890722134L;

	/** the shape of this way object, can be a {@link Polygon}, {@link LineString},
	 * or even a {@link Point}.
	 */
	private Geometry outline;
	
    /** map with key - value pairs for this object. (tags in OSM) */ 
    private Map<String,String> tags;

    /** the OSM-ID of this object or -1 */
    private long id;

    /**
     * the values which are altered an should be written to OSM. 
     */
    private Map<String,String> changedTags;
    
    /**
     * the tags which should be removed from OSM.
     */
    private Set<String> removed;
    
	/**
     * returns a fillColor depending of the value of a given key.
     */
    public int getFillColor(String key) {
    	String tagValue = getValue(key);
    	if(tagValue!= null) {
    		if(tagValue.equalsIgnoreCase("yes"))
    			return 0x7F00AA00;
    		if(tagValue.equalsIgnoreCase("no"))
    			return 0x7FAA0000;
    		if(tagValue.equalsIgnoreCase("limited"))
    			return 0x4FAAAA00;
    	}
        return 0x7FAAAAAA;
    }

	/**
     * returns a stroke color depending of the value of a given key.
     */
    public int getStrokeColor(String key) {
        return getFillColor(key) + 0x30000000;
    }

    /**
     * Checks if the shape of this object is valid.
     * @return <code>true</code> if valid, <code>false</code> if not
     */
    public boolean isValid() {
    	if(outline==null) return false;
    	return outline.isValid();
    }

	/**
     * Standartkonstruktor, welcher alle nötigen Werte initialisiert.
     */
    public Way(Geometry g) {
        tags = new TreeMap<String, String>();
        changedTags = new TreeMap<String, String>();
        removed = new TreeSet<String>();
        outline = g;
        this.id = -1;
    }
    
	/**
     * fügt der Map ein weiteres key-Value pair hinzu.
     * sollte der Key schon vorhanden sein, dann wird dieser überschrieben.
     */
    @Deprecated
    public void addTag(String key, String value) {
        tags.put(key,value);
    }
    
    public void addOriginalTag(String key, String value) {
        tags.put(key,value);
    }

    public void alterTag(String key, String value) {
    	if(removed.contains(key))
    		removed.remove(key);
    	changedTags.put(key, value);
    }
    
    /**
     * Entfernt ein key-Value Paar aus der Map. Sollte es nicht vohanden sein, so wird kein Fehler geworfen.
     */
    public void removeTag(String key) {
    	removed.add(key);
    	changedTags.remove(key);
    }
    
    public Set<String> getRemoved() {
    	return removed;
    }

    /**
     * gibt eine Map mit allen Key-Value paaren zurück.
     */
    public Map<String,String> getTags() {
        return tags;
    }
    
    public Map<String,String> getChangedTags() {
    	return changedTags;
    }

	/**
     * gibt die Value zu dem gegebenen Key zurück oder null, wenn dieser nicht vorhanden ist.
     */
    public String getValue(String key) {
    	if(removed.contains(key))
    		return null;
    	if(changedTags.containsKey(key))
    		return changedTags.get(key);
        return tags.get(key);
    }

    /**
     * Checks if the shape of this way is closed polygon
     * @return <code>true</code> if polygon, <code>false</code> if not
     */
    public boolean isPolygon(){
    	return outline instanceof Polygon;
    }
    
    /**
     * Checks if the shape of this is a series of lines (e.g. {@link LineString})
     * and not a closed polygon.
     * @return <code>true</code> if sequence of lines, <code>false</code> otherwise
     */
    public boolean isLineString(){
    	return outline instanceof LineString;
    }
    
    /**
     * Checks if the shape of this object is just a single point
     * @return <code>true</code> if point, <code>false</code> otherwise
     */
    public boolean isPoint(){
    	return outline instanceof Point;
    }
    
    public Coordinate[] getPoints(){
    	return outline.getCoordinates();
    }
    
    public double distanceTo(Geometry d) {
    	if(outline==null) return 0;
    	return outline.distance(d);
    }


    /**
     * Returns the area of the bounding box.
     * @return area of the bounding box
     */
    public double getArea() {
    	if(outline == null) return 0;
    	return outline.getArea();
    }
    
    
    /**
     * gibt die OSM-ID des Objektes zurück.
     * @return OSM-ID
     */
	public long getId() {
		return id;
	}

	/**
	 * setzt die OSM-ID dieses Objektes.
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Returns the current shape.
	 * @return current shape.
	 */
	public Geometry getGeometry(){
		return outline;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Way) 
			return this.id == ((Way) obj).id;
		return false;
	}

	
	public int compareTo(Way o) {
		return (int) (this.id - o.id);
	}
}
