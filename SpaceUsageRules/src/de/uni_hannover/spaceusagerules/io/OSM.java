package de.uni_hannover.spaceusagerules.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

/**
 * Created by gumulka on 10/10/14.
 */
public class OSM {

	private static boolean buffer = false;

	public static void useBuffer(boolean use) {
		buffer = use;
	}

	@Deprecated
	public static Set<Way> getObjectList(Coordinate c) {
		return getObjectList(c, (float) 0.0005);
	}

	public static Set<Way> getObjectList(Coordinate c, float radius) {
		if (buffer) {
			String filename = String.format(Locale.GERMAN,
					"buffer/%02.4f_%02.4f_%02.4f.xml", c.latitude, c.longitude,
					radius);
			return getObjectList(c, radius, new File(filename));
		}
		return getObjectList(c, radius, null, null);
	}

	@Deprecated
	public static Set<Way> getObjectList(Coordinate c, File f) {
		boolean b = buffer;
		buffer = true;
		Set<Way> back = getObjectList(c, (float) 0.0005, f);
		buffer = b;
		return back;
	}

	public static Set<Way> getObjectList(Coordinate c, float radius, File f) {
		return getObjectList(c, radius, f, null);
	}

	public static Set<Way> getObjectList(Coordinate c, float radius,
			String tagname) {
		return getObjectList(c, radius, null, tagname);
	}

	public static Set<Way> getObjectList(Coordinate c, float radius, File f,
			String tagname) {
		Set<Way> newObjects = new TreeSet<Way>();
		Map<Long, Coordinate> coords = new TreeMap<Long, Coordinate>();
		String connection = null;
		if (tagname == null)
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newObjects;
	}

	public static int alterContent(Set<Way> ways, Coordinate location) {
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
				return -5;
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
