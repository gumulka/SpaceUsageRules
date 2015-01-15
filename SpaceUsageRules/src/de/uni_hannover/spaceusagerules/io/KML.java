package de.uni_hannover.spaceusagerules.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

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
            "<tessellate>1</tessellate>\n";
    /** the third part of the kml-data */
    static final String third ="</Polygon>\n" +
            "</Placemark>\n" +
            "</Document>\n" +
            "</kml>\n";


    /**
     * reads the coordinates from a kml-file an returns them in a polyline.
     * @param kml a filepointer to a file, which should have valid kml
     * @return a polyline containing all extracted coordinates.
     */
    public static Geometry loadKML(File kml) {
        Document doc = null;
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
        try {
			doc = Jsoup.parse(kml, "UTF-8");
	        Element e = doc.select("coordinates").first();
	        for(String c : e.text().split(" ")) { // we can split by space, since it is not allowed to have spaces between coordinates and Jsoup makes a space for every newline
	        	// see also <a href="https://developers.google.com/kml/documentation/kmlreference#coordinates">https://developers.google.com/kml/documentation/kmlreference#coordinates</a>
	            String[] bla = c.split(",");
	            double lon = Double.parseDouble(bla[0]);
	            double lat = Double.parseDouble(bla[1]);
	            // there can be an optional third parameter, which is the altitude, but we don't use it
	            coordinates.add(new Coordinate(lon,lat));
	        }
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        // TODO make clear it is always a polygon
        Coordinate[] coords = new Coordinate[coordinates.size()];
        int i = 0;
        for(Coordinate c : coordinates)
        	coords[i++] = c;
        return new GeometryFactory().createPolygon(coords);
    }

    
    /**
     * Makes valid kml from the given Geometry and writes it to the given file. 
     * @param p Geometry to write
     * @param f the file to write to.
     * @throws IOException all exceptions encountered on trying to write the file.
     */
    public static void writeKML(Geometry p, File f) throws UnsupportedEncodingException, IOException {
    	writeKML(p, "", f);
    }
    
    /**
     * Makes valid kml from the given Geometry and writes it to the given file. 
     * @param p Geometry to write
     * @param name a name to be shown in google earth
     * @param f the file to write to.
     * @throws IOException all exceptions encountered on trying to write the file.
     */
    public static void writeKML(Geometry p, String name, File f) 
    		throws IOException {
    	OutputStream os = new FileOutputStream(f);
    	BufferedOutputStream bos = new BufferedOutputStream(os);
    	String kml = writeKML(p, name);
    	bos.write(kml.getBytes("UTF-8"));
    	bos.close();
    	os.close();
    }
    
    /**
     * Makes valid kml from the given Geometry. 
     * @param p Geometry-object to be converted to a kml string.
     * @param name a name to be shown in google earth
     * @return String containing valid kml
     */
    public static String writeKML(Geometry p, String name) {
    	//if there is only one Geometry, it has to be the outline
    	if(p.getNumGeometries()==1){
	        return first + name + second + writeBoundaryKML(p,"outer") + third;
    	}
    	//if there are more than one Geometries, then one is the outer border 
    	//and the others are the inner borders
    	else{
    		String output = first+name+second;
    		//first write the outer border
    		Geometry outline = p.getBoundary();
    		output += writeBoundaryKML(outline, "outer");
    		
    		//then write the holes
    		for(int i=0;i<p.getNumGeometries();i++){
    			if(p.getGeometryN(i) == outline) continue;
    			
    			output += writeBoundaryKML(p.getGeometryN(i), "inner");
    			
    		}
    		
    		output += third;
    		return output;
    	}
    }
    
    /**
     * Produces a String of KML format, that contains one ring of coordinates. 
     * It can be either the outline of the Polygon or the outline of a hole in the polygon.
     * @param r Geometry that consists only of one polygon
     * @param inOrOut a String that determines if the polygon is an outer or inner border. It has to be either "outer" or "inner" to produce correct KML.
     * @return piece of KML String containing one ring of coordinates
     */
    private static String writeBoundaryKML(Geometry r, String inOrOut){
        String ring = "<"+inOrOut+"BoundaryIs>\n" +
        		"<LinearRing>\n" +
                "<coordinates>\n";
        
        for(Coordinate l : r.getCoordinates()) {
        	//y=latitude, x=longitude
            ring += l.x + "," + l.y + " \n";
        }
        
        ring += "</coordinates>\n" +
        "</LinearRing>\n"+
        "</"+inOrOut+"BoundaryIs>\n";
        return ring;
    }
    
    
}
