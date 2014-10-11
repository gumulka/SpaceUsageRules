package de.uni_hannover.spaceusagerules.core;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gumulka on 10/10/14.
 */
public class Way {

    private List<LatLng> coordinates;
    private Map<String,String> tags;
    private String name;

    public int getFillColor() {
        return 0x7F999999;
    }

    public int getStrokeColor() {
        return getFillColor() + 0x30000000;
    }

    public boolean isValid() {
        return coordinates!= null && tags != null;
    }

    public Way(String name) {
        this.name = name;
        coordinates = new LinkedList<LatLng>();
        tags = new TreeMap<String, String>();
    }

    public Way() {
        coordinates = new LinkedList<LatLng>();
        tags = new TreeMap<String, String>();
        this.name = "";
    }

    public void addCoordinate(LatLng c) {
        coordinates.add(c);
    }

    public void addTag(String key, String value) {
        tags.put(key,value);
        name = key + " -> " + value;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public Map<String,String> getTags() {
        return tags;
    }

    public String getValue(String key) {
        return tags.get(key);
    }

    public boolean isArea() {
        return isValid() && coordinates.get(0).equals(coordinates.get(coordinates.size()-1));
    }

    public String toString() {
        return name;
    }
}
