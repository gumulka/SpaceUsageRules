package de.uni_hannover.spaceusagerules.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

import de.uni_hannover.spaceusagerules.core.Way;

/**
 * Handling communication with OpenStreetMap. Providing really easy to use functions.
 * @author Fabian Pflug
 */
public class OSM {

	/** if the local filebuffer is to be used. Deactivated by default */
	private static boolean buffer = false;
	
	private static GeometryFactory gf = new GeometryFactory();

	/** to reduce the load on the OSM-Server, it is possible to use a local filebuffer 
	 * 
	 * @param use if the buffer is to be used.
	 */
	public static void useBuffer(boolean use) {
		buffer = use;
	}

	/**
	 * the simplest way to get a Set of OSM-objects is by just giving a coordinate.
	 * @param c the coordinate where data should be fetched around.
	 * @return a collection of OSM-Objects.
	 */
	public static Collection<Way> getObjectList(Coordinate c) {
		return getObjectList(c, (float) 0.0005);
	}

	/**
	 * you can also define the (bounding box) radius around the point to retrieve data.
	 * @param c the coordinate where data should be fetched around.
	 * @param radius a radius around this point.
	 * @return a collection of OSM-Objects.
	 */
	public static Collection<Way> getObjectList(Coordinate c, float radius) {
		if (buffer) {
			String filename = String.format(Locale.GERMAN,
					"buffer/%02.4f_%02.4f_%02.4f.xml", c.y, c.x,
					radius);
			return getObjectList(c, radius, new File(filename));
		}
		return getObjectList(c, radius, null, null);
	}

	/**
	 * retrieving Data from the File or saving to it if not existing.
	 * @param c the coordinate where data should be fetched around.
	 * @param f the file to read or save data to
	 * @return a collection of OSM-Objects.
	 */
	public static Collection<Way> getObjectList(Coordinate c, File f) {
		boolean b = buffer;
		buffer = true;
		Collection<Way> back = getObjectList(c, (float) 0.0005, f);
		buffer = b;
		return back;
	}
	/**
	 * retrieving Data from the File or saving to it if not existing.
	 * if it does not exist, the data ist fetched with the given radius
	 * @param c the coordinate where data should be fetched around.
	 * @param radius a radius around this point.
	 * @param f the file to read or save data to
	 * @return a collection of OSM-Objects.
	 */
	public static Collection<Way> getObjectList(Coordinate c, float radius, File f) {
		return getObjectList(c, radius, f, null);
	}

	/**
	 * you can provide a tagname to reduce the output to. Then the OverpassApi server are used.
	 * 
	 * @param c the coordinate where data should be fetched around.
	 * @param radius a radius around this point.
	 * @param tagname a tagname to reduce the Output to.
	 * @return a collection of OSM-Objects.
	 */
	public static Collection<Way> getObjectList(Coordinate c, float radius,
			String tagname) {
		return getObjectList(c, radius, null, tagname);
	}
	
	/**
	 * fetches the data from the OpenStreetMap servers or the local files if available and indicated by buffer.
	 * @param c the coordinate where data should be fetched around.
	 * @param radius a radius around this point.
	 * @param f the file to read or save data to
	 * @param tagname a tagname to reduce the Output to.
	 * @return a document containing the Data.
	 */
	private static Document fetchData(Coordinate c, float radius, File f, String tagname) {
		String connection = null;
		if (tagname == null) // there is no single Tag given. Fetch all the Data!
			connection = "http://api.openstreetmap.org/api/0.6/map?bbox="
					+ (c.x - radius) + "," + (c.y - radius)
					+ ',' + (c.x + radius) + ","
					+ (c.y + radius);
		else // the overpass API is only useful, when we have only one tag.
			connection = "http://www.overpass-api.de/api/xapi?way[bbox="
					+ (c.x - radius) + "," + (c.y - radius)
					+ ',' + (c.x + radius) + ","
					+ (c.y + radius) + "][" + tagname + "=*]";
		Document doc = null;
		try {
			if (buffer && f != null && f.exists() && f.canRead()) {
				// if the file is readable and we use a buffer, read it.
				doc = Jsoup.parse(f, "UTF-8");
			} else {
				// else fetch new.
				Connection.Response res = Jsoup.connect(connection).timeout(10000)
						.userAgent("InMa")
						.followRedirects(true).execute();
				doc = res.parse();
				// write it to buffer if enabled and possible
				if (buffer && f != null && f.getParentFile().canWrite()) {
					FileWriter fw = new FileWriter(f);
					BufferedWriter bfw = new BufferedWriter(fw);
					bfw.write(res.body());
					bfw.close();
					fw.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * parses a Node and puts the result in the static nodeMap
	 * @param e an element containing a valid OSM node object
	 * @return the parsed element.
	 */
	private static Coordinate parseNode(Element e) {
		long id = Long.parseLong(e.attr("id"));
		float lon = Float.parseFloat(e.attr("lon"));
		float lat = Float.parseFloat(e.attr("lat"));
		Coordinate c = new Coordinate(lon, lat);
		coordList.put(id, c);
		return c;
	}
	
	/**
	 * parses a way and puts the result in the static wayMap
	 * @param e an element containing a valid OSM node object
	 * @return the parsed element
	 */
	private static Way parseWay(Element e) {
		Elements el = e.select("nd");
		Coordinate[] penis = new Coordinate[el.size()];
		long wayID = Long.parseLong(e.attr("id"));
		// dereference the previously extracted nodes to have the coordinates
		int i = 0;
		for (Element x : el) {
			long id = Long.parseLong(x.attr("ref"));
			Coordinate c = coordList.get(id);
			if(c == null) {
				String filename = String.format(Locale.GERMAN,
						"buffer/Node_%d.xml",id);
				File f = new File(filename);
				Document doc = null;
				try {
					if (buffer && f != null && f.exists() && f.canRead()) {
						// if the file is readable and we use a buffer, read it.
						doc = Jsoup.parse(f, "UTF-8");
					} else {
						String connection = "http://api.openstreetmap.org/api/0.6/node/" + id;
						org.jsoup.Connection.Response res = Jsoup.connect(connection).timeout(10000)
							.userAgent("InMa")
							.followRedirects(true).execute();
						doc = res.parse();
						// write it to buffer if enabled and possible
						if (buffer && f != null && f.getParentFile().canWrite()) {
							FileWriter fw = new FileWriter(f);
							BufferedWriter bfw = new BufferedWriter(fw);
							bfw.write(res.body());
							bfw.close();
							fw.close();
						}
					}
					Element node = doc.select("node").first();
					c  = parseNode(node);
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
	//		if(c != null)
				penis[i++] = c;
		}
		Way w = null;
		Geometry geo = null;
		try {
			geo = gf.createPolygon(penis);
		} catch (IllegalArgumentException ex) {
			geo = gf.createLineString(penis);
		}
		w = new Way(geo);
		// add metadata like tags and ID
		w.setId(wayID);
		for (Element x : e.select("tag")) {
			w.addOriginalTag(x.attr("k"), x.attr("v"));
		}
		wayList.put(wayID,w);
		return w;
	}
	
	/**
	 * gets a way from the static wayList, or if it is not there, retrieves it from the OSM-servers.
	 * after getting it, it is deleted from the static wayList
	 * @param id the id of the way.
	 * @return the way object
	 */
	private static Way getAndDeleteWay(long id) {
		if(wayList.containsKey(id)) {
			return wayList.remove(id);
		}
		String filename = String.format(Locale.GERMAN,
				"buffer/Way_%d.xml",id);
		File f = new File(filename);
		Document doc = null;
		try {
			if (buffer && f != null && f.exists() && f.canRead()) {
				// if the file is readable and we use a buffer, read it.
				doc = Jsoup.parse(f, "UTF-8");
			} else {
				String connection = "http://api.openstreetmap.org/api/0.6/way/" + id;
				org.jsoup.Connection.Response res = Jsoup.connect(connection).timeout(10000)
					.userAgent("InMa")
					.followRedirects(true).execute();
				doc = res.parse();
				// write it to buffer if enabled and possible
				if (buffer && f != null && f.getParentFile().canWrite()) {
					FileWriter fw = new FileWriter(f);
					BufferedWriter bfw = new BufferedWriter(fw);
					bfw.write(res.body());
					bfw.close();
					fw.close();
				}
			}
			Element e = doc.select("way").first();
			Way w  = parseWay(e);
			if(w==null)
				System.err.println("NNAAAAIIIINNNNN!!! " + id);
			wayList.remove(id);
			return w;
		} catch (IOException e) {
		}

		return null;
	}
	
	/**
	 * creates a multipolygon from a relation.
	 * a multipolygon has an outer shell and inner holes.
	 * @param e an element containing a valid OSM-relation with tag: type->multipolygon
	 * @return returns a new Way containing the multipolygon and all tags of its children.
	 */
	private static Way createMultipoligon(Element e) {
		Elements members = e.select("member[type=way]");
		LinearRing outer = null;
		Map<String,String> tags = new TreeMap<String,String>();
		Elements outerElements =members.select("member[role=outer]");
		if(outerElements.size()>1) {
			List<Way> outerList = new LinkedList<Way>();
			for(Element out : outerElements){
				Way w = getAndDeleteWay(Long.parseLong(out.attr("ref")));
				outerList.add(w);
			}
			int size = 0;
			for(Way w : outerList)
				size += w.getPoints().length;
			Coordinate[] allOuterPoints = new Coordinate[size];
			int i = 0;
			for(Way w : outerList)
				for(Coordinate c : w.getPoints())
					allOuterPoints[i++] = c;
			outer = gf.createLinearRing(allOuterPoints);
		} else if (outerElements.size() == 1) {
			long memberID = Long.parseLong(outerElements.first().attr("ref"));
			Way w = getAndDeleteWay(memberID);
			outer = gf.createLinearRing(w.getPoints());
			for(Entry<String,String> entry : w.getTags().entrySet())
				tags.put(entry.getKey(), entry.getValue());
		}
		Elements innerElements = members.select("member[role=inner]");
		LinearRing[] inner = new LinearRing[innerElements.size()];
		int i = 0;
		for(Element mem : innerElements) {
			long memberID = Long.parseLong(mem.attr("ref"));
			Way w = getAndDeleteWay(memberID);
			if(w==null) {
				System.err.println("BLALLAA");
				return null;
			}
			inner[i++] = gf.createLinearRing(w.getPoints());
			for(Entry<String,String> entry : w.getTags().entrySet())
				tags.put(entry.getKey(), entry.getValue());
		}
		Way w = new Way(gf.createPolygon(outer, inner));
		for(Entry<String,String> entry : tags.entrySet())
			w.addOriginalTag(entry.getKey(), entry.getValue());
		wayList.put((long) wayList.size(), w);
		for(Element el : e.select("tag")) {
			if(!el.attr("k").equalsIgnoreCase("type"))
				w.addOriginalTag(el.attr("k"), el.attr("v"));
		}
		return w;
	}
	
	/**
	 * a list of all Way's fetched in this call.
	 */
	private static Map<Long,Way> wayList;
	/**
	 * a list of all Node's fetched in this call.
	 */
	private static Map<Long, Coordinate> coordList;
	
	/**
	 * fetches and parses data from OpenStreetMap.
	 * 
	 * @param c the coordinate where data should be fetched around.
	 * @param radius a radius around this point.
	 * @param f the file to read or save data to
	 * @param tagname a tagname to reduce the Output to.
	 * @return a collection of OSM-Objects.
	 */
	public static synchronized Collection<Way> getObjectList(Coordinate c, float radius, File f,
			String tagname) {
		wayList = new TreeMap<Long,Way>();
		coordList = new TreeMap<Long, Coordinate>();
		Document doc = fetchData(c, radius, f, tagname);

			// extract all nodes and their Values
			for (Element e : doc.select("node")) {
				parseNode(e);
			}
			// extract all ways
			for (Element e : doc.select("way")) {
				parseWay(e);
			}
			
			// handling relations
			for(Element e : doc.select("relation")) {
				Elements typeE = e.select("tag[k=type]");
				if(typeE.size()==1) {
					if(typeE.first().attr("v").equalsIgnoreCase("multipolygon")) {
						createMultipoligon(e);
					}
				}
			}
			/*
			for(Element e : doc.select("relation")) {
				System.out.println("----------");
				for(Element t : e.select("tag"))
					System.out.println(t);
			} */
			List<Long> delete = new LinkedList<Long>();
			for(Way w : wayList.values()) {
				if(w.getTags().containsKey("building:part")) {
					for(Way other : wayList.values()) {
						if(w.equals(other))
							continue;
						Map<String,String> tagset = other.getTags();
						// if we are part of a building and the other part covers us.
						if(tagset.containsKey("building")) {
							if(other.getGeometry().contains(w.getGeometry())) {
								for(String key : w.getTags().keySet()) {
									if(!tagset.containsKey(key))
										tagset.put(key, w.getValue(key));
								}
								delete.add(w.getId());
								break;
							}
						} else
						// we are connectet to another object which is also a part, so maybe, we belong together.
						if(tagset.containsKey("building:part")) {
							if(other.getGeometry().touches(w.getGeometry())) {
								Geometry intersection = other.getGeometry().intersection(w.getGeometry());
								other.setGeometry(intersection);
								w.setGeometry(intersection);
							}
						}
					}
				}
			}
			for(Long l : delete) {
				wayList.remove(l);
			}
			
			// Adding tags from Points, which are inside a Polygon.
			for (Element e : doc.select("node")) {
				Elements el = e.select("tag");
				if(el.size()==0)
					continue;
				float lon = Float.parseFloat(e.attr("lon"));
				float lat = Float.parseFloat(e.attr("lat"));
				Coordinate coord =  new Coordinate(lon, lat);
				Point p = gf.createPoint(coord);
				// select the smallest object where we are inside
				Way best = null;
				for(Way w : wayList.values()) {
					if(w.getGeometry().contains(p)) {
						if(best == null || best.getArea()>w.getArea())
							best = w;
					}
				}
				// and add the tags if it does exist.
				if(best!= null)
				for (Element x : el) {
					if(!best.getTags().containsKey(x.attr("k")))
						best.addOriginalTag(x.attr("k"), x.attr("v"));
				}
			}
			
		return wayList.values();
	}

	/**
	 *  writes all the altert content in the collection of ways to Openstreetmap.
	 *  
	 * @param ways a list of ways which are altert
	 * @param location the coordinate of the user.
	 * @return 1 on success. a negative Value otherwise.
	 */
	public static int alterContent(Collection<Way> ways, Coordinate location) {
		int status = 0;
		Set<String> concerned = new TreeSet<String>();
		for (Way w : ways) {
			if (w.getChangedTags().size() == 0)
				continue;
			concerned.addAll(w.getChangedTags().keySet());
			String tagid = "";
			for (Entry<String, String> e : w.getChangedTags().entrySet())
				tagid += e.getKey() + "->" + e.getValue() + ";";
			Connection con = Jsoup.connect("http://www.sur.gummu.de/add.php")
					.method(Method.POST);
			String coords = "";
			for (Coordinate c : w.getPoints()) {
				coords += c.y + "," + c.x + ";";
			}
			con.data("coords", coords);
			con.data("id", "" + w.getId());
			con.data("tag", tagid);
			con.data("standort", location.y + "," + location.x);

			try {
				con.execute();
				if (status == 0)
					status = 1;
			} catch (IOException e) {
				status = -1;
			}
		}
		if (status == 0)
			return status;
		String conc = "";
		for (String c : concerned)
			conc += c + ", ";
		OAuthService service = new ServiceBuilder().provider(OSMOauth.class)
				.apiKey("aqfuSUHbSVZU3IpFFX0SSfvrxgEbVjpVaiCEendn")
				.apiSecret("9SgQG6L9N1Y8DdQtYyLL1YqqY1z9ydavWKTN1mDX").build();
		Token accessToken = new Token(
				"0db0H7M7y7fa6uzBwTKzTUasRMgCdWGwIM7uB3f0",
				"O6JF3p96dmAcyHObQJZDooi6AfAonqRl3qP7vDzv");

		OAuthRequest request = new OAuthRequest(Verb.PUT,
				"https://api.openstreetmap.org/api/0.6/changeset/create");
		request.addHeader("Content-type", "text/xml");
		request.addPayload("<osm version='0.6' generator='GummuForOSM'><changeset><tag k=\"created_by\" v=\"GummuForOSM\"/>"
				+ "<tag k=\"comment\" v=\"Adding some tags concerning "
				+ conc
				+ "\"/></changeset></osm>");
		service.signRequest(accessToken, request);
		Response response = request.send();
		if (response.getCode() != 200)
			return -2;
		String changesetID = response.getBody();
		for (Way w : ways) {
			if (w.getChangedTags().size() == 0)
				continue;
			if (w.getId() == -1) {
				List<String> nodeIDs = new LinkedList<String>();
				for(Coordinate c : w.getPoints()) {
					String payload = "<osm>" +
							"<node changeset=\"" + changesetID + "\" lat=\"" + c.y + "\" lon=\"" + c.x + "\">" +
							"</node>" +
							"</osm>";
					request = new OAuthRequest(Verb.PUT,
							"https://api.openstreetmap.org/api/0.6/node/create");
					request.addPayload(payload);
					request.addHeader("Content-type", "text/xml");
					service.signRequest(accessToken, request);
					response = request.send();
					if (response.getCode() != 200) {
						System.err.println(payload);
						System.err.println(w.getId());
						System.err.println(response.getCode());
						System.err.println(response.getBody());
						System.err.println(response.getHeaders());
						return -5;
					}
					nodeIDs.add(response.getBody());
				}
				
				String payload = "<osm>\n" +
						"<way changeset=\"" + changesetID + "\" >\n";
				for(String s : nodeIDs) {
					payload += " <nd ref=\"" + s + "\"/>\n";
				}
				for (String key : w.getChangedTags().keySet()) {
						// Wert wird neu erstellt.
					payload += " <tag k=\"" + key + "\" v=\""
							+ w.getValue(key) + "\" />\n";
				}
				payload += "</way>" +
						"</osm>";
				request = new OAuthRequest(Verb.PUT,
						"https://api.openstreetmap.org/api/0.6/way/create");
				request.addPayload(payload);
				request.addHeader("Content-type", "text/xml");
				service.signRequest(accessToken, request);
				response = request.send();
				if (response.getCode() != 200) {
					System.err.println(payload);
					System.err.println(w.getId());
					System.err.println(response.getCode());
					System.err.println(response.getBody());
					System.err.println(response.getHeaders());
					return -4;
				}
			} else {
				request = new OAuthRequest(Verb.GET,
						"http://api.openstreetmap.org/api/0.6/way/" + w.getId());
				response = request.send();
				String wayBody = response.getBody();
				int end = wayBody.indexOf("</way>");
				if (end < 0)
					return -3;
				String last = wayBody.substring(end);
				wayBody = wayBody.substring(0, end);
				int cha = wayBody.indexOf('"', wayBody.indexOf("changeset")) + 1;
				wayBody = wayBody.substring(0, cha) + changesetID
						+ wayBody.substring(wayBody.indexOf('"', cha));
				cha = wayBody.indexOf('"', wayBody.indexOf("user")) + 1;
				wayBody = wayBody.substring(0, cha) + "Gumulka"
						+ wayBody.substring(wayBody.indexOf('"', cha));
				cha = wayBody.indexOf('"', wayBody.indexOf("uid")) + 1;
				wayBody = wayBody.substring(0, cha) + "2387983"
						+ wayBody.substring(wayBody.indexOf('"', cha));
				cha = wayBody.indexOf('"', wayBody.indexOf("timestamp")) + 1;
				Date d = new Date();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				wayBody = wayBody.substring(0, cha) + df.format(d)
						+ wayBody.substring(wayBody.indexOf('"', cha));
				for(String key : w.getRemoved()) {
					int tagstart = wayBody.lastIndexOf("<tag",
							wayBody.indexOf(key));
					int tagend = wayBody.indexOf('>', tagstart) + 1;
					wayBody = wayBody.substring(0, tagstart) + wayBody.substring(tagend);
				}
				for (String key : w.getChangedTags().keySet()) {
					if (w.getTags().containsKey(key)) {
						// es war bereits vorher ein Wert da.
						int tagstart = wayBody.lastIndexOf("<tag",
								wayBody.indexOf(key));
						int value = wayBody.indexOf("v=\"", tagstart) + 3;
						String endstring = wayBody.substring(wayBody.indexOf(
								'"', value));
						wayBody = wayBody.substring(0, value) + w.getValue(key)
								+ endstring;
					} else {
						// Wert wird neu erstellt.
						wayBody += "<tag k=\"" + key + "\" v=\""
								+ w.getValue(key) + "\" />\n";
					}
				}
				request = new OAuthRequest(Verb.PUT,
						"https://api.openstreetmap.org/api/0.6/way/"
								+ w.getId());
				request.addPayload(wayBody + last);
				request.addHeader("Content-type", "text/xml");
				service.signRequest(accessToken, request);
				response = request.send();
				if (response.getCode() != 200) {
					System.err.println(wayBody + last);
					System.err.println(w.getId());
					System.err.println(response.getCode());
					System.err.println(response.getBody());
					System.err.println(response.getHeaders());
					status = -4;
				}
			}
		}
		request = new OAuthRequest(Verb.PUT,
				"http://api.openstreetmap.org/api/0.6/changeset/" + changesetID
						+ "/close");
		service.signRequest(accessToken, request);
		response = request.send();
		return status;
	}

}
