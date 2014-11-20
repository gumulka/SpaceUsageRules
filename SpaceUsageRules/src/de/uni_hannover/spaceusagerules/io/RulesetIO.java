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

public class RulesetIO {
	
	private static final String RULE_TAG = "rule";
	private static final String RULESET_TAG="ruleset";
	private static final String RESTRICTION_TAG="restriction";
	private static final String OSM_TAG="OSMTag";
	private static final String WEIGHT_ATTRIB="weight";
	private static final String THRESHOLD_ATTRIB="threshold";
	private static final String RADIUS_ATTRIB="radius";	
	
	//TODO javadoc
	public static List<Rules> readRules(String filename) throws IOException{
		File f = new File(filename);
		return readRules(f);
	}
	
	public static List<Rules> readRules(File filename) throws IOException{
		
		if(!filename.exists())
			return null;
		
		Document doc = readDocument(filename);
		
		List<Rules> outputList = new Vector<Rules>();
		
		Elements ruleList = doc.getElementsByTag(RULE_TAG);
		for(Element singleRule : ruleList){
			outputList.add(createRules(singleRule));
		}
		
		return outputList;
	}
	
	
	
	
	//TODO javadoc
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
	
	//TODO javadoc
	private static Document readDocument(File f) throws IOException{
		
		String xmlText="";
		BufferedReader br = new BufferedReader(new FileReader(f));		
		String line;
		while((line=br.readLine()) != null){
			xmlText+=line;
		}
		
		br.close();
		
		return Jsoup.parse(xmlText);
	}
	
	
	//TODO javadoc
	public static String toXMLString(List<Rules> ruleList){
		
		String output = "<html><body>\n<"+RULESET_TAG+">\n";
		
		for(Rules rule: ruleList){
			output += ruleToString(rule);
		}
		
		output += "</"+RULESET_TAG+">\n</body></html>";
		
		return output;
	}
	
	//TODO javadoc
	private static String ruleToString(Rules rule){
		
		Collection<String> surs = rule.getRestrictions();
		Map<String,Double> weights = rule.getWeights();
		Map<String,double[]> thresholds = rule.getThresholds();
		
		String ruleXml = "\t<"+RULE_TAG+">\n";
		
		for(String restriction : surs)
				ruleXml += "\t\t<"+RESTRICTION_TAG+">"+restriction+"</"+RESTRICTION_TAG+">\n";
		
		for(String osmTag : weights.keySet()){
			ruleXml += "\t\t<"+OSM_TAG+" "+WEIGHT_ATTRIB+"=\""+weights.get(osmTag)+"\"";
			if(thresholds.containsKey(osmTag)){
				ruleXml +=" "+THRESHOLD_ATTRIB+"=\""+thresholds.get(osmTag)[0]+"\""+
						" "+RADIUS_ATTRIB+"=\""+thresholds.get(osmTag)[1]+"\"";
			}
			ruleXml += ">"+osmTag+"</"+OSM_TAG+">\n";
		}
		ruleXml += "\t</"+RULE_TAG+">\n";
		
		return ruleXml;
	}
	

}
