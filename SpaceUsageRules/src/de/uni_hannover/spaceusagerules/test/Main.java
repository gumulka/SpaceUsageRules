package de.uni_hannover.spaceusagerules.test;

import java.io.File;
import java.io.IOException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ImageProcessingException
	 */
	public static void main(String[] args) throws ImageProcessingException,
			IOException {
		// TODO Auto-generated method stub
		System.out.println("Dieses Projekt ist nicht zum ausführen gedacht.");
		for (int x = 1; x <= 96; x++) {
			System.out.print(x);
			String filename = String.format("assets/%04d.jpg", x);
			File f = new File(filename);
			Metadata meta = ImageMetadataReader.readMetadata(f);
			for (Directory d : meta.getDirectories()) {
				for (Tag t : d.getTags()) {
					if (t.getTagName().equalsIgnoreCase("GPS Latitude")) {
						Rational[] values = d.getRationalArray(t.getTagType());
						double lat = values[0].doubleValue();
						lat += values[1].doubleValue() / 60;
						lat += values[2].doubleValue() / 3600;
						System.out.print(" " + lat);
					}
					if (t.getTagName().equalsIgnoreCase("GPS Longitude")) {
						Rational[] values = d.getRationalArray(t.getTagType());
						double lat = values[0].doubleValue();
						lat += values[1].doubleValue() / 60;
						lat += values[2].doubleValue() / 3600;
						System.out.println(" " + lat);
					}
				}
			}
		}
	}

}
