package de.uni_hannover.spaceusagerules.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Polyline;

/**
 * A class to read in and write out KML-data from files.
 * @author Fabian Pflug
 */
public class KML {

	/** the first part of the kml-data */
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
	/** the second part of the kml-data */
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
    /** the third part of the kml-data */
    static final String third = "</coordinates>\n" +
            "</LinearRing>\n" +
            "</outerBoundaryIs>\n" +
            "</Polygon>\n" +
            "</Placemark>\n" +
            "</Document>\n" +
            "</kml>\n";


    /**
     * reads the coordinates from a kml-file an returns them in a polyline.
     * @param kml a filepointer to a file, which should have valid kml
     * @return a polyline containing all extracted coordinates.
     */
    public static Polyline loadKML(File kml) {
        Document doc = null;
        Polyline coordinates = new Polyline();
        try {
			doc = Jsoup.parse(kml, "UTF-8");
	        Element e = doc.select("coordinates").first();
	        for(String c : e.text().split(" ")) { // we can split by space, since it is not allowed to have spaces between coordinates and Jsoup makes a space from every newline
	        	// see also <a href="https://developers.google.com/kml/documentation/kmlreference#coordinates">https://developers.google.com/kml/documentation/kmlreference#coordinates</a>
	            String[] bla = c.split(",");
	            double lon = Double.parseDouble(bla[0]);
	            double lat = Double.parseDouble(bla[1]);
	            // there can be an optional third parameter, which is the altitude, but we don't use it
	            coordinates.add(new Coordinate(lat,lon));
	        }
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        return coordinates;
    }

    
    /**
     * makes valid kml from the given coordinates and writes it to the given file 
     * @param coordinates list of coordinates
     * @param f the file to write to.
     * @throws IOException all exceptions encountered on trying to write the file.
     */
    public static void writeKML(Polyline p, File f) throws UnsupportedEncodingException, IOException {
    	writeKML(p, "", f);
    }
    
    /**
     * makes valid kml from the given coordinates and writes it to the given file 
     * @param coordinates list of coordinates
     * @param name a name to be shown in google earth
     * @param f the file to write to.
     * @throws IOException all exceptions encountered on trying to write the file.
     */
    public static void writeKML(Polyline p, String name, File f) 
    		throws IOException {
    	OutputStream os = new FileOutputStream(f);
    	BufferedOutputStream bos = new BufferedOutputStream(os);
    	String kml = writeKML(p, name);
    	bos.write(kml.getBytes("UTF-8"));
    	bos.close();
    	os.close();
    }
    
    /**
     * makes valid kml from the given coordinates and writes it to the given file 
     * @param coordinates list of coordinates
     * @param name a name to be shown in google earth
     * @return String containing valid kml
     */
    public static String writeKML(Polyline p, String name) {
        String coords = "";
        for(Coordinate l : p.getPoints()) {
            coords += l.latitude + "," + l.longitude + " \n";
        }
        return first + name + second + coords + third;
    }
}
