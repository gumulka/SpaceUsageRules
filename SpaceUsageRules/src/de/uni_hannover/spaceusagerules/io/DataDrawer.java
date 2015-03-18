package de.uni_hannover.spaceusagerules.io;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.algorithm.Start;
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
	
	double scale;
	
	/** Direction which the photographer was facing */
	double direction;
	
	/**
	 * Creates a DataDrawer for a specific location.
	 * @param width width of the picture
	 * @param height height of the picture
	 * @param middle center of the area to be rendered
	 */
	public DataDrawer(int width, int height, Coordinate middle, double scale, double direction){
		this.width = width;
		this.height = height;
		this.location = middle;
		this.scale = scale;
		this.direction = direction;
		
		//create image
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gr = image.getGraphics();

		((Graphics2D) gr).setStroke(new BasicStroke(4));
		
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
		drawWay(w, Color.DARK_GRAY);
	}
	
	/**
	 * draws the given string in with a bigger font then usual on top of the image.
	 * @param rules
	 */
	public void drawRules(String rules) {
		gr.setColor(Color.black);
		int startoffset = 0;
		Font f = gr.getFont();
		Font big = f.deriveFont((float) f.getSize()+20);
		gr.setFont(big);
		do { // this is to clip overlength rules to a next line
			int last = rules.length();
			if(last>200) // if longer then 200 charakters, then clip
				last = rules.lastIndexOf(' ',200);
			String print = rules.substring(0, last);
			if(rules.length()>last+1)
				rules = rules.substring(last+1);
			else
				rules = "";
			gr.drawString(print, width/10 , height/20+startoffset);
			startoffset += 30;
		}while(rules.length() != 0);
		gr.setFont(f);
	}
	
	/**
	 * draws a single Ring or a single Polygon into the image.
	 * @param lr Polygon to draw
	 */
	private void drawRing(LineString lr) {
		//create drawable polygon
		int[] ints;
		Polygon polygon = new Polygon();
		
		for(Coordinate c : lr.getCoordinates()){
			ints = transformToInt(c);
			polygon.addPoint(ints[0]+margin, ints[1]+margin);
		}
			
		//draw polygon outline
		gr.drawPolygon(polygon);
	}
	
	public void drawgeometry(Geometry w, Color color) {
		gr.setColor(color);

		if(w instanceof com.vividsolutions.jts.geom.Polygon) {
			com.vividsolutions.jts.geom.Polygon pol = (com.vividsolutions.jts.geom.Polygon) w;
			drawRing(pol.getExteriorRing());
			// XXX vielleicht noch eine gestrichelte Linie hinzufügen um zu zeigen, dass wir eine inner Linie sind.
			for(int i = 0; i<pol.getNumInteriorRing();i++)
				drawRing(pol.getInteriorRingN(i));
		}
	}
	
	/**
	 * Draws a {@link Way} with a specific outline color.
	 * @param w the Way to be drawn
	 * @param color color of the outline
	 */
	public void drawWay(Way w, Color color) {
		gr.setColor(color);
		
		if(w.getGeometry() instanceof com.vividsolutions.jts.geom.Polygon) {
			com.vividsolutions.jts.geom.Polygon pol = (com.vividsolutions.jts.geom.Polygon) w.getGeometry();
			drawRing(pol.getExteriorRing());
			// XXX vielleicht noch eine gestrichelte Linie hinzufügen um zu zeigen, dass wir eine inner Linie sind.
			for(int i = 0; i<pol.getNumInteriorRing();i++)
				drawRing(pol.getInteriorRingN(i));
		}
		
		
		//draw center
		Point p = w.getGeometry().getCentroid();
		int[] center = transformToInt(p.getCoordinate());
		if(center[1]>height)
			center[1] = height *95 / 100;
		if(center[0]>width)
			center[0] = width *95 / 100;
		if(center[1]<0)
			center[1] = height *5 / 100;
		if(center[0]<0)
			center[0] = width *5 / 100;
		gr.setColor(color);
		gr.drawLine(center[0]-3, center[1], center[0]+3, center[1]);
		gr.drawLine(center[0], center[1]-3, center[0], center[1]+3);
		
		List<String> tag = new Vector<String>();
		for(String key : w.getTags().keySet()){
			tag.add(key+"->"+w.getTags().get(key));
		}
//		tag = filterTags(tag);
		gr.setColor(Color.BLACK);
		drawTags(tag,center[0],center[1]);
		
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
		output[0] = (int)((p.x-location.x)/(scale)*(double)(width-2*margin)) + width/2;
		output[1] = height/2 - (int)((p.y-location.y)/(scale)*(double)(width-2*margin));
		
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
			if(w.isPolygon())
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
			if(w.isPolygon())
				drawWay(w);
		}
		
		
		//draw unmoved reference point
		if(Start.includeOrientation && direction!=Double.NaN){
			gr.setColor(new Color(0,100,0));
			
			//move the reference point back to it's original position
			int[] oldref = transformToInt(Image.move(location, -direction, Image.DISTANCE_TO_SIGN));
			
			gr.drawOval(oldref[0]-11, oldref[1]-11, 22, 22);
			gr.fillOval(oldref[0]-10, oldref[1]-10, 20, 20);

		}
		
		
		//draw reference point p in a special manner
		int[] ref = transformToInt(location);
		gr.setColor(Color.DARK_GRAY);
		gr.drawOval(ref[0]-11, ref[1]-11, 22, 22);
		gr.fillOval(ref[0]-10, ref[1]-10, 20, 20);
		
		//if wanted draw direction in which the photo was taken
		if(Start.includeOrientation){
			gr.setColor(new Color(0,100,0));
			if(direction == Double.NaN){
				//a dark green circle shows, that there is no direction, that can be drawn
				gr.drawOval(ref[0]-12, ref[1]-12, 24, 24);
			}
			else{
				int toX = ref[0] + (int)(30.*Math.cos(direction));
				int toY = ref[1] + (int)(30.*Math.sin(direction));
				
				//make a thick line
				for(int i=-1;i<2;i++)
					for(int j=-1;j<2;j++)
						gr.drawLine(ref[0]+i, ref[1]+j, toX+i, toY+j);
				//XXX make it look more like an arrow
			}
		}
		
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
