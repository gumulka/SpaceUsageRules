package de.uni_hannover.spaceusagerules.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

/**
 * @todo Javadoc schreiben.
 * @author Peter Zilz
 *
 */
public class DataDrawer {
	
	private Color polygonFillColor = Color.white;
	
	private int width, height;
	private int margin = 5;
	private String format = "png";
	private Graphics gr;
	private BufferedImage image;
	
	private Coordinate location;
	
	public DataDrawer(int width, int height, Coordinate middle){
		this.width = width;
		this.height = height;
		this.location = middle;
		
		//Bild erstellen
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gr = image.getGraphics();
		
		//adjust font size
		Font font = gr.getFont();
		gr.setFont(font.deriveFont(9f));
		
		//Hintergrungd zeichnen
		gr.setColor(Color.orange);
		gr.fillRect(0, 0, width, height);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	
	public void drawWay(Way w){
		drawWay(w, Color.black);
	}
	public void drawWay(Way w, Color color) {
		
		//create drawable polygon
		int[] ints;
		Polygon polygon = new Polygon();
		for(Coordinate c : w.getPolyline().getPoints()){
			ints = transformToInt(c);
			polygon.addPoint(ints[0]+margin, ints[1]+margin);
		}
			
		//draw and fill polygon
//		gr.setColor(new Color(w.getFillColor()));
//		gr.fillPolygon(polygon);
		gr.setColor(color);
		gr.drawPolygon(polygon);
		
		//draw center (of bounding box)
		Rectangle boundingBox = polygon.getBounds();
		int centerX = boundingBox.width/2 + boundingBox.x;
		int centerY = boundingBox.height/2 + boundingBox.y;
		if(centerY>height)
			centerY = height *95 / 100;
		if(centerX>width)
			centerX = width *95 / 100;
		if(centerY<0)
			centerY = height *5 / 100;
		if(centerX<0)
			centerX = width *5 / 100;
		gr.setColor(color);
		gr.drawLine(centerX-3, centerY, centerX+3, centerY);
		gr.drawLine(centerX, centerY-3, centerX, centerY+3);
		
		
		//TODO draw Tags
		List<String> tag = new Vector<String>();
		for(String key : w.getTags().keySet()){
			tag.add(key+"->"+w.getTags().get(key));
		}
//		tag = filterTags(tag);
		gr.setColor(Color.BLACK);
		drawTags(tag,centerX,centerY);
		
	}
	
	@SuppressWarnings("unused")
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
	
	
	private void drawTags(List<String> tags, int x, int y){
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
		output[0] = (int)((p.longitude-location.longitude)/(0.002)*(double)(width-2*margin)) + width/2;
		output[1] = height/2 - (int)((p.latitude-location.latitude)/(0.002)*(double)(width-2*margin));
//		output[0] = (int)((p.longitude-minX)/(maxX-minX)*(double)(width-2*margin));
//		output[1] = (int)((-p.latitude+maxY)/(maxX-minX)*(double)(height-2*margin));
		
		return output;
	}
	
	
	private List<Way> retrieveData(Coordinate p, float radius){
		List<Way> output = new LinkedList<Way>(); 
		for(Way w : OSM.getObjectList(p, radius))
			if(w.isArea())
				output.add(w);
		return output;
	}
	
	
	public void render(float radius){
		List<Way> data = retrieveData(location, radius);
		render(data);
	}
	
	public void render(Collection<Way> data) {
		
		//polygone zeichnen
		for(Way w : data){
			drawWay(w);
		}
		
		//draw reference point p
		int[] ref = transformToInt(location);
		gr.setColor(Color.DARK_GRAY);
		gr.drawOval(ref[0]-6, ref[1]-6, 12, 12);
		gr.fillOval(ref[0]-5, ref[1]-5, 10, 10);
		
	}
	
	
	public void saveImage(String name) throws IOException
	{
		
		///@todo optional: create metadata
		
		FileOutputStream fos = new FileOutputStream(name);
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
		
		ImageWriter iw = ImageIO.getImageWritersByFormatName(format).next();
		iw.setOutput(ios);
		iw.write(image);
		
		ios.close();
		fos.close();
		
	}

	public Color getPolygonFillColor() {
		return polygonFillColor;
	}



	public void setPolygonFillColor(Color polygonFillColor) {
		this.polygonFillColor = polygonFillColor;
	}

}
