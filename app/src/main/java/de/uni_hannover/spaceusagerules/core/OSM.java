package de.uni_hannover.spaceusagerules.core;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gumulka on 10/10/14.
 */
public class OSM {

    private static List<Way> Objects;

    public static List<Way> getWays() {
        return Objects;
    }

    public static void createObjectList(Location c){
        createObjectList(c, (float) 0.0005);
    }

    public static void createObjectList(Location c, float radius){
        List<Way> newObjects = new LinkedList<Way>();
        Map<Long,LatLng> coords = new TreeMap<Long,LatLng>();
        String connection = "http://openstreetmap.org/api/0.6/map?bbox="
                + (c.getLongitude()-radius) + "," + (c.getLatitude()-radius) + ','
                + (c.getLongitude()+radius) + "," + (c.getLatitude()+radius);
        try {
            Connection.Response res = Jsoup.connect(connection).userAgent("InMa SpaceUsageRules").followRedirects(true).execute();
            Document doc = res.parse();
            for(Element e : doc.select("node")){
                long  id = Long.parseLong(e.attr("id"));
                float lon = Float.parseFloat(e.attr("lon"));
                float lat = Float.parseFloat(e.attr("lat"));
                coords.put(id,new LatLng(lat, lon));
            }
            for(Element e : doc.select("way")){
                Way w = new Way();
                w.addTag("visible", e.attr("visible"));
                for(Element x : e.select("nd")) {
                    long id = Long.parseLong(x.attr("ref"));
                    w.addCoordinate(coords.get(id));
                }
                for(Element x : e.select("tag")) {
                    w.addTag(x.attr("k"), x.attr("v"));
                }
                newObjects.add(w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Objects = newObjects;
    }
}
