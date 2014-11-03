package de.uni_hannover.spaceusagerules.core;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Klasse zur Representation eines Weg-Objektes in OSM.
 */
public class Way implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4356641146890722134L;

    /** Die representation des Polygons, bzw der Polyline, welche zu diesem Weg gehören. */
	Polyline coordinates;
    /** Map mit einer Zuordnung von Key - Value paaren in OSM-Tags. */ 
    private Map<String,String> tags;
    /** Ein Optionaler Name, welcher gesetzt werden kann und für die Darstellung bei toString() benutzt wird. */
    private String name;
    /** Die OSM-ID des Objektes */
    private long id;

    private Map<String,String> changedTags;
    
    private Set<String> removed;
    
	/**
     * Gibt eine Füllfarbe abhängig von Tag's zurück
     */
    @Deprecated
    public int getFillColor() {
        if("truth".equalsIgnoreCase(tags.get("InformatiCup")))
            return 0x7F00AA00;
        if("guess".equalsIgnoreCase(tags.get("InformatiCup")))
            return 0x7FAA0000;
        if("true".equalsIgnoreCase(tags.get("sur:clicked")))
            return 0x7F0000AA;
        if("green".equalsIgnoreCase(tags.get("InMaColor")))
            return 0x7F00AA00;
        if("red".equalsIgnoreCase(tags.get("InMaColor")))
            return 0x7FAA0000;
    	if(tags.containsKey("building"))
    		return 0x4FAAAA00;
        return 0x7F999999;
    }

	/**
     * Gibt eine Füllfarbe in Abhängigkeit von einem bestimmten Key in der Tagmap zurück.
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
     * Gibt eine Randfarbe abhängig von Tag's zurück
     */
    @Deprecated
    public int getStrokeColor() {
        return getFillColor() + 0x30000000;
    }

	/**
     * Gibt eine Randfarbe in Abhängigkeit von einem bestimmten Key in der Tagmap zurück.
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
}
