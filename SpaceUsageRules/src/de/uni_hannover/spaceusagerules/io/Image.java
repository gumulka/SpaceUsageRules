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

import de.uni_hannover.spaceusagerules.core.Coordinate;

/**
 * @todo Javadoc ins englische umschreiben.
 * Klasse zum Einlesen von Bild-Metadaten.
 * 
 * @author Fabian Pflug
 *
 */
public class Image {
	
	/**
	 * List die Datei ein und extrahiert aus dem Metadaten die Coordinaten, an denen das Bild aufgenommen wurde, wenn vorhanden.
	 * @param filename der Dateiname des Bildes.
	 * @return Die GPS Position an der es aufgenommen wurde.
	 * @throws ImageProcessingException
	 * @throws IOException
	 */
	public static Coordinate readCoordinates(String filename) throws ImageProcessingException, IOException {
		return readCoordinates(new File(filename));
	}

	/**
	 * List die Datei ein und extrahiert aus dem Metadaten die Coordinaten, an denen das Bild aufgenommen wurde, wenn vorhanden.
	 * @param f Der Dateizeiger zum Bild.
	 * @return Die GPS Position an der es aufgenommen wurde.
	 * @throws ImageProcessingException
	 * @throws IOException
	 */
	public static Coordinate readCoordinates(File f) throws ImageProcessingException, IOException {
		InputStream is = new FileInputStream(f);
		return readCoordinates(is);
	}
	
	/**
	 * List die Datei ein und extrahiert aus dem Metadaten die Coordinaten, an denen das Bild aufgenommen wurde, wenn vorhanden.
	 * @param is Ein Stream, aus welchem ein Bild gelesen werden kann.
	 * @return Die GPS Position an der es aufgenommen wurde.
	 * @throws ImageProcessingException
	 * @throws IOException
	 */
	public static Coordinate readCoordinates(InputStream is) throws ImageProcessingException, IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		Metadata meta = ImageMetadataReader.readMetadata(bis, false);
		double lat = 0, lon=0;
		double lat_dir = 1, lon_dir= 1;
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
			}
		}
		return new Coordinate(lat*lat_dir, lon*lon_dir);

	}
}
