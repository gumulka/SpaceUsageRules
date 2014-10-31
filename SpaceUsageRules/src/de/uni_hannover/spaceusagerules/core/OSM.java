package de.uni_hannover.spaceusagerules.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;


/**
 * Created by gumulka on 10/10/14.
 */
public class OSM {

    private static boolean buffer = false;

    public static void useBuffer(boolean use) {
    	buffer = use;
    }


    public static List<Way> getObjectList(Coordinate c){
        return getObjectList(c, (float) 0.0005);
    }

    public static List<Way> getObjectList(Coordinate c, float radius) {
    	if(buffer) {
    		String filename = String.format(Locale.GERMAN, "buffer/%02.4f_%02.4f_%02.4f.xml",c.latitude, c.longitude, radius);
    		return getObjectList(c, radius, new File(filename));
    	}
    	return getObjectList(c, radius, null);
    }

    public static List<Way> getObjectList(Coordinate c, File f) {
    	boolean b = buffer;
    	buffer = true;
    	List<Way> back = getObjectList(c, (float) 0.0005, f);
    	buffer = b;
    	return back;
    }

    public static List<Way> getObjectList(Coordinate c, float radius, File f){
        List<Way> newObjects = new LinkedList<Way>();
        Map<Long,Coordinate> coords = new TreeMap<Long,Coordinate>();
        String connection = "http://api.openstreetmap.org/api/0.6/map?bbox="
                + (c.longitude-radius) + "," + (c.latitude-radius) + ','
                + (c.longitude+radius) + "," + (c.latitude+radius);
        System.out.println(connection);
        try {
        	Document doc = null;
    		if(buffer && f!= null && f.exists() && f.canRead()) {
    			doc = Jsoup.parse(f, "UTF-8");
        	} else {
        		Connection.Response res = Jsoup.connect(connection).userAgent("InMa SpaceUsageRules").followRedirects(true).execute();
        		doc = res.parse();
        		if(buffer && f!=null) {
        			FileWriter fw = new FileWriter(f);
        			BufferedWriter bfw = new BufferedWriter(fw);
        			bfw.write(res.body());
        			bfw.close();
        			fw.close();
        		}
        	}
            for(Element e : doc.select("node")){
                long  id = Long.parseLong(e.attr("id"));
                float lon = Float.parseFloat(e.attr("lon"));
                float lat = Float.parseFloat(e.attr("lat"));
                coords.put(id,new Coordinate(lat, lon));
            }
            for(Element e : doc.select("way")){
                Way w = new Way();
                long wayID = Long.parseLong(e.attr("id"));
                w.setId(wayID);
                w.addTag("visible", e.attr("visible"));
                for(Element x : e.select("nd")) {
                    long id = Long.parseLong(x.attr("ref"));
                    w.addCoordinate(coords.get(id));
                }
                for(Element x : e.select("tag")) {
                    w.addTag(x.attr("k"), x.attr("v"));
                }
                newObjects.add(w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newObjects;
    }

    public static void main(String... args) {
    	OSM.useBuffer(true);
    	Coordinate Uni = new Coordinate(52.33695,9.66405);
    	Long wayID = (long) 0;
    	for(Way w : OSM.getObjectList(Uni)) {
    		if(w.getTags().containsValue("4"))
    			wayID = w.getId();
    	}   // */

    	OAuthService service = new ServiceBuilder()
        .provider(OSMOauth.class)
        .apiKey("aqfuSUHbSVZU3IpFFX0SSfvrxgEbVjpVaiCEendn")
        .apiSecret("9SgQG6L9N1Y8DdQtYyLL1YqqY1z9ydavWKTN1mDX")
        .build();

//    	Token requestToken = service.getRequestToken();

  //  	System.out.println(service.getAuthorizationUrl(requestToken));
//    	Scanner in = new Scanner(System.in);
 //   	Verifier verifier = new Verifier(in.nextLine());
    	// Token für die richtige API
    	Token accessToken = new Token("abFhlFMa4gxDH4ERRndbe90sCD42yd0BwPlAIepf","jBUNwxSRT4U79nT7hJaSMdLsUzS2HFowBXkuDGb8");
    	// Token für die dev API.
//    	Token accessToken = new Token("PrzbafTYb2EcRJktor1FGnOWvr77TuzbduBsl8gc","HkiZPrBXhX4Wyt2dyRsilZkmNulC7JA8C9cJDA5t");

//    	Token accessToken = service.getAccessToken(requestToken, verifier);
    	OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.openstreetmap.org/api/0.6/user/details");
    	service.signRequest(accessToken, request);
    	Response response = request.send();
    	System.out.println(response.getBody());
    	System.out.println(accessToken);
    	request = new OAuthRequest(Verb.GET, "http://api.openstreetmap.org/api/0.6/way/" + wayID);
    	response = request.send();
    	String bla = response.getBody(); 
   // 	System.out.println(bla);
    	int end = bla.indexOf("</way>");
    	String last = bla.substring(end);
    	bla = bla.substring(0, end);
    	System.out.println(bla + "<tag k=\"wheelchair\" v=\"no\" />\n" + last);

    	request = new OAuthRequest(Verb.PUT, "http://api.openstreetmap.org/api/0.6/changeset/create");
    	request.addPayload("<osm><changeset><tag k=\"created_by\" v=\"GummuForOSM\"/>" +
    			"<tag k=\"comment\" v=\"Adding some Tags concerning wheelchair\"/></changeset></osm>");
    	service.signRequest(accessToken, request);

    	response = request.send();
    	System.out.println(response.getCode());
    	System.out.println(response.getHeaders());
    	System.out.println("Changeset ID = " + response.getBody());

    	/* Connection con = Jsoup.connect("http://www.openstreetmap.org/api/0.6/changeset/create");
    	con.data("changeset", "<osm><changeset><tag k=\"created_by\" v=\"GummuForOSM\"/></changeset></osm>");
    	con.method(Method.POST);
    	Response res = null;
    	try{
    		res = con.execute();
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.println(e.getMessage());
    	}
    	if(res !=null)
    		System.out.println(res.body());
    	// */
    }
}
