package de.uni_hannover.spaceusagerules.core;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

	/**
     * Gibt eine Füllfarbe abhängig von Tag's zurück
     */
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
    	String tagValue = tags.get(key);
    	if(tagValue!= null) {
    		if(tagValue.equalsIgnoreCase("yes"))
    			return 0x7F00AA00;
    		if(tagValue.equalsIgnoreCase("no"))
    			return 0x7FAA0000;
    		if(tagValue.equalsIgnoreCase("partly"))
    			return 0x4FAAAA00;
    	}
        return 0x7F999999;
    }

	/**
     * Gibt eine Randfarbe abhängig von Tag's zurück
     */
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
        this.name = name;
        coordinates = new Polyline();
        tags = new TreeMap<String, String>();
    }

	/**
     * Standartkonstruktor, welcher alle nötigen Werte initialisiert.
     */
    public Way() {
        coordinates = new Polyline();
        tags = new TreeMap<String, String>();
        this.name = "";
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
    public void addTag(String key, String value) {
        tags.put(key,value);
        name = key + " -> " + value;
    }
    
    /**
     * Entfernt ein key-Value Paar aus der Map. Sollte es nicht vohanden sein, so wird kein Fehler geworfen.
     */
    public void removeTag(String key) {
    	tags.remove(key);
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

	/**
     * gibt die Value zu dem gegebenen Key zurück oder null, wenn dieser nicht vorhanden ist.
     */
    public String getValue(String key) {
        return tags.get(key);
    }

	/**
     * gibt an, ob es sich um eine Polyline oder ein Polygon handelt.
     */
    @Deprecated
    public boolean isArea() {
        return coordinates.isArea();
    }
    
    /**
     * gibt die Fläche der Coordinaten zurück.
     */
    @Deprecated
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
}