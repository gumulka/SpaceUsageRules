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
 * {@link DataDrawer} is a tool to visualize the polygons and their tags around any place.
 * The two main purposes are to familiarize with the existing kinds of tags and to 
 * see how good (or bad) the implementation works. 
 * @author Peter Zilz
 *
 */
public class DataDrawer {
	
	/** width and height of the image */
	private int width, height;
	/** distance from content to frame */
	private int margin = 5;
	/** the format with which the picture is saved */
	private String format = "png";
	
	/** Graphics of {@link #image} to draw on */
	private Graphics gr;
	/** to store the rendered picture */
	private BufferedImage image;
	
	/** the center of the area that is to be rendered */ 
	private Coordinate location;
	
	/**
	 * Creates a DataDrawer for a specific location.
	 * @param width width of the picture
	 * @param height height of the picture
	 * @param middle center of the area to be rendered
	 */
	public DataDrawer(int width, int height, Coordinate middle){
		this.width = width;
		this.height = height;
		this.location = middle;
		
		//create image
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gr = image.getGraphics();
		
		//adjust font size
		Font font = gr.getFont();
		gr.setFont(font.deriveFont(9f));
		
		//draw background; orange is easier on the eyes
		gr.setColor(Color.orange);
		gr.fillRect(0, 0, width, height);
	}
	
	/**
	 * Returns the rendered image. If it has not been rendered yet, it is blank.
	 * @return rendered (or blank) image
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Draws a {@link Way} in the default Color {@link Color#black}.
	 * Just calls {@link #drawWay(Way, Color)}.
	 * @param w the Way to be drawn
	 */
	public void drawWay(Way w){
		drawWay(w, Color.black);
	}
	
	/**
	 * Draws a {@link Way} with a specific outline color.
	 * @param w the Way to be drawn
	 * @param color color of the outline
	 */
	public void drawWay(Way w, Color color) {
		
		//create drawable polygon
		int[] ints;
		Polygon polygon = new Polygon();
		for(Coordinate c : w.getPolyline().getPoints()){
			ints = transformToInt(c);
			polygon.addPoint(ints[0]+margin, ints[1]+margin);
		}
			
		//draw polygon outline
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
		
		
		List<String> tag = new Vector<String>();
		for(String key : w.getTags().keySet()){
			tag.add(key+"->"+w.getTags().get(key));
		}
//		tag = filterTags(tag);
		gr.setColor(Color.BLACK);
		drawTags(tag,centerX,centerY);
		
	}
	
	/**
	 * Filters the list of tags of one element. That way unnecessary text doesn't show up
	 * on the picture. For example the tag "visible->true" is not very helpful. 
	 * @param input list of tags to be filtered 
	 * @return list of tags without unnecessary entries
	 */
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
	
	/**
	 * Draws the list of tags as text at a specified location.
	 * @param tags list of tags to be drawn
	 */
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
	
	/**
	 * Downloads the needed data from OSM and removes all non polygons. 
	 * @param p center of the area
	 * @param radius radius of the area
	 * @return List of Polygons
	 */
	private List<Way> retrieveData(Coordinate p, float radius){
		List<Way> output = new LinkedList<Way>(); 
		for(Way w : OSM.getObjectList(p, radius))
			if(w.isArea())
				output.add(w);
		return output;
	}
	
	/**
	 * Downloads and renders the area around the given {@link #location}.
	 * @param radius radius of the area
	 */
	public void render(float radius){
		List<Way> data = retrieveData(location, radius);
		render(data);
	}
	
	/**
	 * Renders a specific set of data.
	 * @param data List of Polygons with tags
	 */
	public void render(Collection<Way> data) {
		
		//draw polygons
		for(Way w : data){
			if(w.isArea())
				drawWay(w);
		}
		
		//draw reference point p in special manner
		int[] ref = transformToInt(location);
		gr.setColor(Color.DARK_GRAY);
		gr.drawOval(ref[0]-6, ref[1]-6, 12, 12);
		gr.fillOval(ref[0]-5, ref[1]-5, 10, 10);
		
	}
	
	/**
	 * Stores the rendered picture. The file extension is not automatically appended. 
	 * @param name name of the file incl. file extension
	 * @throws IOException
	 */
	public void saveImage(String name) throws IOException
	{
		
		///XXX create metadata
		
		FileOutputStream fos = new FileOutputStream(name);
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
		
		ImageWriter iw = ImageIO.getImageWritersByFormatName(format).next();
		iw.setOutput(ios);
		iw.write(image);
		
		ios.close();
		fos.close();
		
	}
	
}
