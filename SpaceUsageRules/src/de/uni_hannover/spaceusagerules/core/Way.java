package de.uni_hannover.spaceusagerules.core;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gumulka on 10/10/14.
 */
public class Way implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4356641146890722134L;
	
	Polyline coordinates;
    private Map<String,String> tags;
    private String name;

    public int getFillColor() {
        if("truth".equalsIgnoreCase(tags.get("InformatiCup")))
            return 0x7F00AA00;
        if("guess".equalsIgnoreCase(tags.get("InformatiCup")))
            return 0x7FAA0000;
        if("true".equalsIgnoreCase(tags.get("sur:clicked")))
            return 0x7F0000AA;
    	if(tags.containsKey("building"))
    		return 0x4FAAAA00;
        return 0x7F999999;
    }

    public int getStrokeColor() {
        return getFillColor() + 0x30000000;
    }

    public boolean isValid() {
        return coordinates.getPoints().size()>1;
    }

    public Way(String name) {
        this.name = name;
        coordinates = new Polyline();
        tags = new TreeMap<String, String>();
    }

    public Way() {
        coordinates = new Polyline();
        tags = new TreeMap<String, String>();
        this.name = "";
    }

    public void addCoordinate(Coordinate c) {
        coordinates.add(c);
    }

    public void addAllCoordinates(Collection<Coordinate> coords) {
        coordinates.addAll(coords);
    }

    public void addTag(String key, String value) {
        tags.put(key,value);
        name = key + " -> " + value;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates.getPoints();
    }

    public Map<String,String> getTags() {
        return tags;
    }

    public String getValue(String key) {
        return tags.get(key);
    }

    public boolean isArea() {
        return coordinates.isArea();
    }
    
    public double getArea() {
    	return coordinates.boundingBoxArea();
    }

    public String toString() {
        return name;
    }
    
    public Polyline getPolyline() {
    	return coordinates;
    }
}
