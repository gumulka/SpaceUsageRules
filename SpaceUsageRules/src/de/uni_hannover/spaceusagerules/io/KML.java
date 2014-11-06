package de.uni_hannover.spaceusagerules.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Polyline;

/**
 * Eine Klasse zum einlesen und Schreiben von KML Dateien.
 * @author Fabian Pflug
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


    /**
     * Lies aus einer Datei die Coordinaten aus und gibt diese als Polyline zurück.
     * @param kml ein Dateizeiger zu einer Datei, welche gültiges KML enthalten sollte.
     * @return eine Polyline mit allen extrahierten Koordinaten.
     */
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

    
    /**
     * Macht aus den Koordinaten gültiges KML und schreibt es in eine Datei raus.
     * @param coordinates Liste von Koordinaten.
     * @param f Die Datei in welche das KML geschrieben werden soll.
     * @throws UnsupportedEncodingException
     * @throws IOException Alle IO-Exceptions, welche beim schreiben geworfen werden.
     */
    public static void writeKML(List<Coordinate> coordinates, File f) throws UnsupportedEncodingException, IOException {
    	writeKML(coordinates, "", f);
    }
    
    /**
     * Macht aus den Koordinaten gültiges KML und schreibt es in eine Datei raus.
     * @param coordinates Liste von Koordinaten.
     * @param name Namen, welcher in Google Earth angezeigt werden soll.
     * @param f Die Datei in welche das KML geschrieben werden soll.
     * @throws UnsupportedEncodingException
     * @throws IOException Alle IO-Exceptions, welche beim schreiben geworfen werden.
     */
    public static void writeKML(List<Coordinate> coordinates, String name, File f) throws UnsupportedEncodingException, IOException {
    	OutputStream os = new FileOutputStream(f);
    	BufferedOutputStream bos = new BufferedOutputStream(os);
    	String kml = writeKML(coordinates, name);
    	bos.write(kml.getBytes("UTF-8"));
    	bos.close();
    	os.close();
    }
    
    /**
     * Macht aus einer Liste von Coordinaten und einem Namen eine KML-Representation.
     * @param coordinates Liste von Koordinaten.
     * @param name Namen, welcher in Google Earth angezeigt werden soll.
     * @return String mit einem gültigen KML-XML
     */
    public static String writeKML(List<Coordinate> coordinates, String name) {
        String coords = "";
        for(Coordinate l : coordinates) {
            coords += l.latitude + "," + l.longitude + "\n";
        }
        return first + name + second + coords + third;
    }
}
