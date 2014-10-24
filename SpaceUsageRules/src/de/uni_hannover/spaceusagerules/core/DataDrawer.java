package de.uni_hannover.spaceusagerules.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class DataDrawer {
	
	private Color polygonFillColor = Color.white;
	
	private double minX, maxX, minY, maxY;
	
	private float radius;
	
	private int width, height;
	private int margin = 5;
	private String format = "png";
	
	
	public DataDrawer(int width, int height, float radius){
		OSM.useBuffer(true);
		this.width = width;
		this.height = height;
		this.radius = radius;
		
	}
	
	public void drawWay(Graphics gr, Way w){
		
		//create drawable polygon
		int[] ints;
		Polygon polygon = new Polygon();
		for(Coordinate c : w.getCoordinates()){
			ints = transformToInt(c);
			polygon.addPoint(ints[0]+margin, ints[1]+margin);
		}
			
		//draw and fill polygon
//		gr.setColor(new Color(w.getFillColor()));
//		gr.fillPolygon(polygon);
		gr.setColor(Color.black);
		gr.drawPolygon(polygon);
		
		//draw center (of bounding box)
		Rectangle boundingBox = polygon.getBounds();
		int centerX = boundingBox.width/2 + boundingBox.x;
		int centerY = boundingBox.height/2 + boundingBox.y;
		gr.setColor(Color.black);
		gr.drawLine(centerX-3, centerY, centerX+3, centerY);
		gr.drawLine(centerX, centerY-3, centerX, centerY+3);
		
		
		//TODO draw Tags
		List<String> tag = new Vector<String>();
		for(String key : w.getTags().keySet()){
			tag.add(key+"->"+w.getTags().get(key));
		}
//		tag = filterTags(tag);
		drawTags(gr,tag,centerX,centerY);
		
	}
	
	private List<String> filterTags(List<String> input){
		
		List<String> output = new Vector<String>();
		String test;
		for(int i=0;i<input.size();i++){
			test = input.get(i);
			if(test.startsWith("visible")) continue;
			output.add(test);
		}
		
		return output;
	}
	
	
	private void drawTags(Graphics gr, List<String> tags, int x, int y){
		
		for(int i=0;i<tags.size();i++){
			gr.drawString(tags.get(i), x, y+(i+1)*11);
		}
		
		
		
	}
	
	
	
	
	/**
	 * Transforms GPS coordinates to pixel positions.
	 * @param p GPS coordinates
	 * @return array of length two. First is <code>x</code> position, second is <code>y</code> position.
	 */
	private int[] transformToInt(Coordinate p){
		
		int[] output = new int[2];
		
		output[0] = (int)((p.longitude-minX)/(maxX-minX)*(double)(width-2*margin));
		output[1] = (int)((-p.latitude+maxY)/(maxX-minX)*(double)(height-2*margin));
		
		return output;
	}
	
	
	private List<Way> retrieveData(Coordinate p){
		List<Way> output = OSM.getObjectList(p, radius);
		return output;
	}
	
	public BufferedImage render(Coordinate p){
		
		List<Way> data = retrieveData(p);
		
		//min & max festlegen
		minX = Double.MAX_VALUE;
		maxX = Double.MIN_VALUE;
		minY = Double.MAX_VALUE;
		maxY = Double.MIN_VALUE;
		for(Way w : data){
			for(Coordinate c : w.getCoordinates()){
				if(c.longitude>maxX) maxX = c.longitude;
				if(c.longitude<minX) minX = c.longitude;
				if(c.latitude>maxY) maxY = c.latitude;
				if(c.latitude<minY) minY = c.latitude;
			}
		}
		
		//Bild erstellen
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics gr = image.getGraphics();
		
		//adjust font size
		Font font = gr.getFont();
		gr.setFont(font.deriveFont(9f));
		
		//Hintergrungd zeichnen
		gr.setColor(Color.orange);
		gr.fillRect(0, 0, width, height);
		
		//polygone zeichnen
		for(Way w : data){
			if(w.isArea())
				drawWay(gr, w);
		}
		
		//draw reference point p
		int[] ref = transformToInt(p);
		gr.setColor(Color.DARK_GRAY);
		gr.drawOval(ref[0]-6, ref[1]-6, 12, 12);
		gr.fillOval(ref[0]-5, ref[1]-5, 10, 10);
		
		
		return image;
	}
	
	
	public void saveImage(String name, BufferedImage image) throws IOException
	{
		
		//TODO optional: create metadata
		
		FileOutputStream fos = new FileOutputStream(name);
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
		
		ImageWriter iw = ImageIO.getImageWritersByFormatName(format).next();
		iw.setOutput(ios);
		iw.write(image);
		
		ios.close();
		fos.close();
		
	}
	
	
	public static void main(String[] args) {
		
		DataDrawer drawer = new DataDrawer(10000,5000, 0.0005f);
		BufferedImage image = drawer.render(new Coordinate(50.9304, 5.33901));
		try {
			drawer.saveImage("001.png", image);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	public Color getPolygonFillColor() {
		return polygonFillColor;
	}



	public void setPolygonFillColor(Color polygonFillColor) {
		this.polygonFillColor = polygonFillColor;
	}

}
