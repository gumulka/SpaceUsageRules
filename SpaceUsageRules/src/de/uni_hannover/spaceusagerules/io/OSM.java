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

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

/**
 * Handling communication with OpenStreetMap. Providing really easy to use functions.
 * @author Fabian Pflug
 */
public class OSM {

	/** if the local filebuffer is to be used. Deactivated by default */
	private static boolean buffer = false;

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
	public static Set<Way> getObjectList(Coordinate c) {
		return getObjectList(c, (float) 0.0005);
	}

	/**
	 * you can also define the (bounding box) radius around the point to retrieve data.
	 * @param c the coordinate where data should be fetched around.
	 * @param radius a radius around this point.
	 * @return a collection of OSM-Objects.
	 */
	public static Set<Way> getObjectList(Coordinate c, float radius) {
		if (buffer) {
			String filename = String.format(Locale.GERMAN,
					"buffer/%02.4f_%02.4f_%02.4f.xml", c.latitude, c.longitude,
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
	public static Set<Way> getObjectList(Coordinate c, File f) {
		boolean b = buffer;
		buffer = true;
		Set<Way> back = getObjectList(c, (float) 0.0005, f);
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
	public static Set<Way> getObjectList(Coordinate c, float radius, File f) {
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
	public static Set<Way> getObjectList(Coordinate c, float radius,
			String tagname) {
		return getObjectList(c, radius, null, tagname);
	}

	/**
	 * fetches data from OpenStreetMap.
	 * 
	 * @param c the coordinate where data should be fetched around.
	 * @param radius a radius around this point.
	 * @param f the file to read or save data to
	 * @param tagname a tagname to reduce the Output to.
	 * @return a collection of OSM-Objects.
	 */
	public static Set<Way> getObjectList(Coordinate c, float radius, File f,
			String tagname) {
		Set<Way> newObjects = new TreeSet<Way>();
		Map<Long, Coordinate> coords = new TreeMap<Long, Coordinate>();
		String connection = null;
		if (tagname == null) // there is no single Tag given. Fetch all the Data!
			connection = "http://api.openstreetmap.org/api/0.6/map?bbox="
					+ (c.longitude - radius) + "," + (c.latitude - radius)
					+ ',' + (c.longitude + radius) + ","
					+ (c.latitude + radius);
		else
			connection = "http://www.overpass-api.de/api/xapi?way[bbox="
					+ (c.longitude - radius) + "," + (c.latitude - radius)
					+ ',' + (c.longitude + radius) + ","
					+ (c.latitude + radius) + "][" + tagname + "=*]";
		try {
			/// TODO hier alles kommentieren.
			Document doc = null;
			if (buffer && f != null && f.exists() && f.canRead()) {
				doc = Jsoup.parse(f, "UTF-8");
			} else {
				Connection.Response res = Jsoup.connect(connection).timeout(10000)
						.userAgent("InMa")
						.followRedirects(true).execute();
				doc = res.parse();
				if (buffer && f != null) {
					FileWriter fw = new FileWriter(f);
					BufferedWriter bfw = new BufferedWriter(fw);
					bfw.write(res.body());
					bfw.close();
					fw.close();
				}
			}
			for (Element e : doc.select("node")) {
				long id = Long.parseLong(e.attr("id"));
				float lon = Float.parseFloat(e.attr("lon"));
				float lat = Float.parseFloat(e.attr("lat"));
				coords.put(id, new Coordinate(lat, lon));
			}
			for (Element e : doc.select("way")) {
				Way w = new Way();
				long wayID = Long.parseLong(e.attr("id"));
				w.setId(wayID);
				for (Element x : e.select("nd")) {
					long id = Long.parseLong(x.attr("ref"));
					w.addCoordinate(coords.get(id));
				}
				for (Element x : e.select("tag")) {
					w.addOriginalTag(x.attr("k"), x.attr("v"));
				}
				newObjects.add(w);
			}
			// Adding tags from Points, wich are inside a Polygon.
			for (Element e : doc.select("node")) {
				Elements el = e.select("tag");
				if(el.size()==0)
					continue;
				float lon = Float.parseFloat(e.attr("lon"));
				float lat = Float.parseFloat(e.attr("lat"));
				Coordinate coord =  new Coordinate(lat, lon);
				Way best = null;
				for(Way w : newObjects) {
					if(w.getPolyline().inside(coord)) {
						if(best == null || best.getArea()>w.getArea())
							best = w;
					}
				}
				if(best!= null)
				for (Element x : el) {
					if(!best.getTags().containsKey(x.attr("k")))
						best.addOriginalTag(x.attr("k"), x.attr("v"));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newObjects;
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
			for (Coordinate c : w.getPolyline().getPoints()) {
				coords += c.toString() + ";";
			}
			con.data("coords", coords);
			con.data("id", "" + w.getId());
			con.data("tag", tagid);
			con.data("standort", location.toString());

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
				for(Coordinate c : w.getPolyline().getPoints()) {
					String payload = "<osm>" +
							"<node changeset=\"" + changesetID + "\" lat=\"" + c.latitude + "\" lon=\"" + c.longitude + "\">" +
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
