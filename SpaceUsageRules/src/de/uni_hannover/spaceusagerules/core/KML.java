package de.uni_hannover.spaceusagerules.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by gumulka on 10/11/14.
 */
public class KML {

    static final String first = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n" +
            "<Document>\n" +
            "<Style id=\"Poly1\">\n" +
            "<LineStyle>\n" +
            "<color>7f00ff00</color>\n" +
            "<width>2</width>\n" +
            "</LineStyle>\n" +
            "<PolyStyle>\n" +
            "<color>7f00ff00</color>\n" +
            "</PolyStyle>\n" +
            "</Style>\n" +
            "<Placemark>\n" +
            "<name>";
    static final String second = "</name>\n" +
            "<description></description>\n" +
            "<styleUrl>#Poly1</styleUrl>\n" +
            "<Polygon>\n" +
            "<altitudeMode>clampToGround</altitudeMode>\n" +
            "<extrude>1</extrude>\n" +
            "<tessellate>1</tessellate>\n" +
            "<outerBoundaryIs>\n" +
            "<LinearRing>\n" +
            "<coordinates>\n";
    static final String third = "</coordinates>\n" +
            "</LinearRing>\n" +
            "</outerBoundaryIs>\n" +
            "</Polygon>\n" +
            "</Placemark>\n" +
            "</Document>\n" +
            "</kml>\n";

 /*   public static List<LatLng> loadKML(InputStream is) {

        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean found = false;
        while ((line = r.readLine()) != null) {
            if(line.contains("<coordinates>")) {
                found = true;
                continue;
            }
            if(line.contains("</coordinates>")) {
                found = true;
                continue;
            }
            total.append(line);
        }
    } */


    public static Polyline loadKML(String kml) {
        Document doc = null;
        Polyline coordinates = new Polyline();
        doc = Jsoup.parse(kml, "UTF-8");
        Element e = doc.select("coordinates").first();
        for(String c : e.text().split(" ")) {
            String[] bla = c.split(",");
            double lon = Double.parseDouble(bla[0]);
            double lat = Double.parseDouble(bla[1]);
            coordinates.add(new Coordinate(lat,lon));
        }
        return coordinates;
    }


    public static Polyline loadKML(File kml) {
        Document doc = null;
        Polyline coordinates = new Polyline();
        try {
			doc = Jsoup.parse(kml, "UTF-8");
	        Element e = doc.select("coordinates").first();
	        for(String c : e.text().split(" ")) {
	            String[] bla = c.split(",");
	            double lon = Double.parseDouble(bla[0]);
	            double lat = Double.parseDouble(bla[1]);
	            coordinates.add(new Coordinate(lat,lon));
	        }
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        return coordinates;
    }

    public static String writeKML(List<Coordinate> coordinates, String name) {
        String coords = "";
        for(Coordinate l : coordinates) {
            coords += l.latitude + "," + l.longitude + "\n";
        }
        return first + name + second + coords + third;
    }
}
