package de.uni_hannover.spaceusagerules.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * Created by gumulka on 10/10/14.
 */
public class OSM {

    private static boolean buffer = false;
    
    public static void useBuffer(boolean use) {
    	buffer = use;
    }

    
    public static List<Way> getObjectList(Coordinate c){
        return getObjectList(c, (float) 0.0005);
    }

    public static List<Way> getObjectList(Coordinate c, float radius) {
    	if(buffer) {
    		String filename = String.format(Locale.GERMAN, "buffer/%02.4f_%02.4f_%02.4f.xml",c.latitude, c.longitude, radius);
    		return getObjectList(c, radius, new File(filename));
    	}
    	return getObjectList(c, radius, null);
    }
    
    public static List<Way> getObjectList(Coordinate c, File f) {
    	boolean b = buffer;
    	buffer = true;
    	List<Way> back = getObjectList(c, (float) 0.0005, f);
    	buffer = b;
    	return back;
    }
    
    public static List<Way> getObjectList(Coordinate c, float radius, File f){
        List<Way> newObjects = new LinkedList<Way>();
        Map<Long,Coordinate> coords = new TreeMap<Long,Coordinate>();
        String connection = "http://openstreetmap.org/api/0.6/map?bbox="
                + (c.longitude-radius) + "," + (c.latitude-radius) + ','
                + (c.longitude+radius) + "," + (c.latitude+radius);
        try {
        	Document doc = null;
    		if(buffer && f!= null && f.exists() && f.canRead()) {
    			doc = Jsoup.parse(f, "UTF-8");
        	} else {
        		Connection.Response res = Jsoup.connect(connection).userAgent("InMa SpaceUsageRules").followRedirects(true).execute();
        		doc = res.parse();
        		if(buffer && f!=null) {
        			FileWriter fw = new FileWriter(f);
        			BufferedWriter bfw = new BufferedWriter(fw);
        			bfw.write(res.body());
        			bfw.close();
        			fw.close();
        		}
        	}
            for(Element e : doc.select("node")){
                long  id = Long.parseLong(e.attr("id"));
                float lon = Float.parseFloat(e.attr("lon"));
                float lat = Float.parseFloat(e.attr("lat"));
                coords.put(id,new Coordinate(lat, lon));
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
        return newObjects;
    }
}
