package de.uni_hannover.spaceusagerules.core;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Representation of an OSM-Object
 * 
 * @todo die Javadoc ins englische umschreiben.
 * @author Fabian Pflug
 */
public class Way implements Serializable, Comparable<Way>{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4356641146890722134L;

    /** the polyline or polygon belonging to this object. */
	Polyline coordinates;
    /** map with key - value pairs for this object. (tags in OSM) */ 
    private Map<String,String> tags;
    /** an optional name. if set it is used for the toString Method */
    private String name;
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
     * return a fillColor depending on tags.
     */
    @Deprecated
    public int getFillColor() {
    	if(tags.containsKey("building"))
    		return 0x4FAAAA00;
        return 0x7F999999;
    }

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
     * return a stroke color depending on tags.
     */
    @Deprecated
    public int getStrokeColor() {
        return getFillColor() + 0x30000000;
    }

	/**
     * returns a stroke color depending of the value of a given key.
     */
    public int getStrokeColor(String key) {
        return getFillColor(key) + 0x30000000;
    }

    /**
     * Eine Methode um zu überprüfen ob es sich um eine gültige Polyline handelt.
     */
    public boolean isValid() {
        return coordinates.getPoints().size()>1;
    }

    /**
     * Konstruktor, welcher es erlaubt direkt einen Namen für dieses Objekt zu definieren.
     */
    public Way(String name) {
        coordinates = new Polyline();
        tags = new TreeMap<String, String>();
        changedTags = new TreeMap<String, String>();
        removed = new TreeSet<String>();
        this.name = name;
        this.id = -1;
    }

	/**
     * Standartkonstruktor, welcher alle nötigen Werte initialisiert.
     */
    public Way() {
        coordinates = new Polyline();
        tags = new TreeMap<String, String>();
        changedTags = new TreeMap<String, String>();
        removed = new TreeSet<String>();
        this.name = "";
        this.id = -1;
    }

	/**
     * erweitert die Polyline um einen weiteren Punkt.
     */
    public void addCoordinate(Coordinate c) {
        coordinates.add(c);
    }

	/**
     * erweitert die Polyline um eine Liste von Punkten 
     */
    public void addAllCoordinates(Collection<Coordinate> coords) {
        coordinates.addAll(coords);
    }

	/**
     * fügt der Map ein weiteres key-Value pair hinzu.
     * sollte der Key schon vorhanden sein, dann wird dieser überschrieben.
     */
    @Deprecated
    public void addTag(String key, String value) {
        tags.put(key,value);
        name = key + " -> " + value;
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
     * Gibt die Liste der Coordinaten zurück, aus welcher diese Polyline besteht.
     */
    @Deprecated
    public List<Coordinate> getCoordinates() {
        return coordinates.getPoints();
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
     * gibt an, ob es sich um eine Polyline oder ein Polygon handelt.
     */
    public boolean isArea() {
        return coordinates.isArea();
    }

    /**
     * gibt die Fläche der Coordinaten zurück.
     */
    public double getArea() {
    	return coordinates.boundingBoxArea();
    }

	/**
     * Gibt eine String-representation des Objekts zurück.
     */
    public String toString() {
        return name;
    }

    /**
     * gibt die Polylinie zurück.
     */
    public Polyline getPolyline() {
    	return coordinates;
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Way) 
			return this.id == ((Way) obj).id;
		return false;
	}

	@Override
	public int compareTo(Way o) {
		return (int) (this.id - o.id);
	}
}
