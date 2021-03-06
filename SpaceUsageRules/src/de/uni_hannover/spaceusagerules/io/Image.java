package de.uni_hannover.spaceusagerules.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.vividsolutions.jts.geom.Coordinate;

import de.uni_hannover.spaceusagerules.algorithm.Start;


/**
 * Class to read in image-metadata and give the geocoordination, where the image was made.
 * 
 * @author Fabian Pflug
 *
 */
public class Image {
	
	/** the estimated distance from smart phone to sign */
	public static double DISTANCE_TO_SIGN = 0.05/60./60.;
	
	
	/**
	 * reads in the file and extracts the geocoordination if possible.
	 * @param filename the filename of the image
	 * @return the gps position where the image was taken.
	 * @throws ImageProcessingException if the metadata can't be read.
	 * @throws IOException if the file can't be read.
	 */
	public static Coordinate readCoordinates(String filename) throws ImageProcessingException, IOException {
		return readCoordinates(new File(filename));
	}

	/**
	 * reads in the file and extracts the geocoordination if possible.
	 * @param f filepointer to the image
	 * @return the gps position where the image was taken.
	 * @throws ImageProcessingException if the metadata can't be read.
	 * @throws IOException if the file can't be read.
	 */
	public static Coordinate readCoordinates(File f) throws ImageProcessingException, IOException {
		InputStream is = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(is);
		Metadata meta = ImageMetadataReader.readMetadata(bis, false);
		double lat = 0, lon=0;
		double lat_dir = 1, lon_dir= 1;
		double orientation = Double.NaN;
		
		for (Directory d : meta.getDirectories()) {
			for (Tag t : d.getTags()) {
				if (t.getTagName().equalsIgnoreCase("GPS Latitude")) {
					Rational[] values = d.getRationalArray(t.getTagType());
					lat = values[0].doubleValue();
					lat += values[1].doubleValue() / 60;
					lat += values[2].doubleValue() / 3600;
				}
				if (t.getTagName().equalsIgnoreCase("GPS Longitude")) {
					Rational[] values = d.getRationalArray(t.getTagType());
					lon = values[0].doubleValue();
					lon += values[1].doubleValue() / 60;
					lon += values[2].doubleValue() / 3600;
				}
				if (t.getTagName().equalsIgnoreCase("GPS Longitude Ref")) {
					if(d.getString(t.getTagType()).equalsIgnoreCase("E"))
						lon_dir = 1;
					else if(d.getString(t.getTagType()).equalsIgnoreCase("W"))
						lon_dir = -1;
				}
				if (t.getTagName().equalsIgnoreCase("GPS Latitude Ref")) {
					if(d.getString(t.getTagType()).equalsIgnoreCase("N"))
						lon_dir = 1;
					else if(d.getString(t.getTagType()).equalsIgnoreCase("S"))
						lon_dir = -1;
				}
				
				//if wanted extract the direction in which the photo was taken
				if(Start.includeOrientation && t.getTagName().equals("GPS Img Direction")){
					Rational value = d.getRational(t.getTagType());

					//convert degrees to rads. 
					orientation = value.doubleValue() / -180. * Math.PI + 1.75*Math.PI;
				}
				
			}
		}
		
		Coordinate output = new Coordinate(lon*lon_dir,lat*lat_dir); 
		
		if(Start.includeOrientation){
			if(orientation != Double.NaN){
				
				//1.5 meters are roughly 0.05 arc seconds
				//within Germany this differs only about a few centimeters.
				output = move(output,orientation,DISTANCE_TO_SIGN);
				
				//the orientation is smuggled with the z-value
				output.z = orientation;
				
				System.out.println(f.getName()+" has GPS Img Direction");
			}
			else
				output.z = Double.NaN;
		}
		
		return output;
	}
	
	/**
	 * Moves a location in a specified direction.
	 * @param input location to move
	 * @param direction direction to move in, measured in rad
	 * @param distance given in degrees. Tricky!
	 * @return moved location
	 */
	public static Coordinate move(Coordinate input, double direction, double distance){
		
		double newX = input.x + Math.cos(direction)*distance;
		double newY = input.y + Math.sin(direction)*distance;
		
		return new Coordinate(newX,newY);
	}
	
}
