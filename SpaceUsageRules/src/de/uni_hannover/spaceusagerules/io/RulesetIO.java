package de.uni_hannover.spaceusagerules.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.uni_hannover.spaceusagerules.algorithm.Rules;

/**
 * This class handles the storing of the ruleset. All rules are stored together in 
 * an XML file. For more information on the structure of this XML file, see the 
 * documentation.<BR>
 * The core functions of this class are {@link #readRules(File)} and 
 * {@link #toXMLString(List)}. The first one reads the file and parses it via Jsoup.
 * The second one parses a list of rules into an xml string, that can be written to a file.<BR>
 * To avoid typos, the names of the used rules are defined as static and final fields.
 * @author Peter Zilz
 *
 */
public class RulesetIO {
	
	/** Name of the xml tag that represents  a whole rule */
	private static final String RULE_TAG = "rule";
	/** Name of the xml tag that contains all the rules */
	private static final String RULESET_TAG="ruleset";
	/** Name of the xml tag that represents a restriction (or space usage rule) */
	private static final String RESTRICTION_TAG="restriction";
	/** Name of the xml tag that represents a tag of a polygon in OSM */
	private static final String OSM_TAG="OSMTag";
	/** Name of the xml attribute of the weight an OSM tag is mapped to */
	private static final String WEIGHT_ATTRIB="weight";
	/** Name of the xml attribute of the threshold an OSM tag is mapped to */
	private static final String THRESHOLD_ATTRIB="threshold";
	/** Name of the xml attribute of the radius an OSM tag is mapped to */
	private static final String RADIUS_ATTRIB="radius";	
	
	/**
	 * Reads and parses an xml file that contains a set of rules.
	 * @param filename name of the file to read and parse
	 * @return list of rules, or <code>null</code> if the file doesn't exist
	 * @throws IOException
	 */
	public static List<Rules> readRules(String filename) throws IOException{
		File f = new File(filename);
		return readRules(f);
	}
	
	/**
	 * Reads and parses an xml file that contains a set of rules.
	 * @param filename the file to read and parse
	 * @return list of rules, or <code>null</code> if the file doesn't exist
	 * @throws IOException
	 */
	public static List<Rules> readRules(File filename) throws IOException{
		
		if(!filename.exists())
			return null;
		
		//read the file and parse from html string to Jsoup objects
		Document doc = Jsoup.parse(filename, "UTF-8");
		
		List<Rules> outputList = new Vector<Rules>();
		
		//parse Jsoup elements to instances of Rules
		Elements ruleList = doc.getElementsByTag(RULE_TAG);
		for(Element singleRule : ruleList){
			outputList.add(createRules(singleRule));
		}
		
		return outputList;
	}
	
	/**
	 * Parses a Jsoup Element into an instance of Rules. 
	 * @param ruleElement Jsoup representation of the rule-tag with subtags
	 * @return new Rules object
	 */
	public static Rules createRules(Element ruleElement){
		
		//create empty datafields
		Collection<String> restrictions = new Vector<String>();
		Map<String,Double> weights = new HashMap<String,Double>();
		Map<String,double[]> thresholds = new HashMap<String,double[]>();
		
		//parse restrictions (=SURs)
		Elements restrictionList = ruleElement.getElementsByTag(RESTRICTION_TAG);
		for(Element singleRestriction : restrictionList){
			restrictions.add(singleRestriction.text());
		}
		
		//parse osm tags and its values
		Elements osmList = ruleElement.getElementsByTag(OSM_TAG);
		String key;
		double[] thr;
		for(Element osmTag : osmList){
			key = osmTag.text();
			weights.put(key,Double.parseDouble(osmTag.attr(WEIGHT_ATTRIB)));
			thr = new double[]{
					Double.parseDouble(osmTag.attr(THRESHOLD_ATTRIB)),
					Double.parseDouble(osmTag.attr(RADIUS_ATTRIB))
			};
			thresholds.put(key, thr);
		}
		
		//put it all together
		return new Rules(restrictions,weights,thresholds);
	}
	
	/**
	 * Parses a list of Rules into an XML string, that can be stored in a file. 
	 * @param ruleList list of rules to be stored.
	 * @return xml string "human readable"
	 */
	public static String toXMLString(List<Rules> ruleList){
		
		//the single rules are surrounded by html-, body-, and rulset-tags
		String output = "<"+RULESET_TAG+">\n";
		
		//parse each rule to xml and append
		for(Rules rule: ruleList){
			output += ruleToString(rule);
		}
		
		output += "</"+RULESET_TAG+">";
		
		return output;
	}
	
	/**
	 * Parses a single rule into xml format. To get a bit of human readability, tabs are
	 * inserted and line breaks are used.
	 * @param rule the rule to parse
	 * @return several lines of xml representing the rule
	 */
	private static String ruleToString(Rules rule){
		
		//read the data from the rule
		Collection<String> surs = rule.getRestrictions();
		Map<String,Double> weights = rule.getWeights();
		Map<String,double[]> thresholds = rule.getThresholds();
		
		//begin xml code
		String ruleXml = "\t<"+RULE_TAG+">\n";
		
		//first append all restrictions (=SURs)
		for(String restriction : surs)
				ruleXml += "\t\t<"+RESTRICTION_TAG+">"+restriction+"</"+RESTRICTION_TAG+">\n";
		
		//append all used osm tags. One tag maps to the three values weight, threshold and
		//radius. Those are attributes in this tag. The name of the OSM-tag is the text of
		//this xml tag.
		for(String osmTag : weights.keySet()){
			ruleXml += "\t\t<"+OSM_TAG+" "+WEIGHT_ATTRIB+"=\""+weights.get(osmTag)+"\"";
			if(thresholds.containsKey(osmTag)){
				ruleXml +=" "+THRESHOLD_ATTRIB+"=\""+thresholds.get(osmTag)[0]+"\""+
						" "+RADIUS_ATTRIB+"=\""+thresholds.get(osmTag)[1]+"\"";
			}
			ruleXml += ">"+osmTag+"</"+OSM_TAG+">\n";
		}
		//close the rule-tag
		ruleXml += "\t</"+RULE_TAG+">\n";
		
		return ruleXml;
	}
	

}
