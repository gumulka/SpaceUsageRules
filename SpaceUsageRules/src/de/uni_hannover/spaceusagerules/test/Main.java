package de.uni_hannover.spaceusagerules.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.drew.imaging.ImageProcessingException;

import de.uni_hannover.spaceusagerules.core.Image;
import de.uni_hannover.spaceusagerules.core.OSM;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ImageProcessingException
	 */
	public static void main(String[] args) throws ImageProcessingException,
			IOException {
		OSM.useBuffer(true);
		String filename = String.format(Locale.GERMAN,"../SpaceUsageRulesVis/assets/%04d.jpg",1);
		InputStream is = new FileInputStream(new File(filename));
		OSM.getObjectList(Image.readCoordinates(is));
	}

}
