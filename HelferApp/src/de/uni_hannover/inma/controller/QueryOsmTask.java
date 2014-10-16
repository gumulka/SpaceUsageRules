package de.uni_hannover.inma.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.location.Location;
import android.os.AsyncTask;
import de.uni_hannover.inma.model.MainActivity;
import de.uni_hannover.inma.model.Tag;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Way;

public class QueryOsmTask extends AsyncTask<Location, Integer, List<Tag>> {
    private MainActivity a;
    private float radius;
    
    
    public QueryOsmTask(MainActivity activity, float radius) {
        this.a = activity;
        this.radius = radius;
    }
    
    @Override
    protected List<Tag> doInBackground(Location... urls) {
        Coordinate l = new Coordinate(urls[0].getLatitude(),urls[0].getLongitude());
        List<Way> ways =  OSM.getObjectList(l, radius);

    	Map<String,Tag> possibilities = new TreeMap<String,Tag>();
    	List<Tag>  tags = new LinkedList<Tag>();
    	for(String line : a.getPossibilities())
            	possibilities.put(line, new Tag(line));
        for(Way w : ways) {
        	for(String s : w.getTags().keySet()) {
        		if(possibilities.keySet().contains(s)) {
        			Tag t = possibilities.get(s);
        			if(t!=null)
        				t.addWay(w);
        		}
        	}
        }
        a.addAreaData(ways);
        for(Tag t : possibilities.values()) {
        	if(!t.isEmpty())
        		tags.add(t);
        }
        return tags;

        
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(List<Tag> ways) {
    	a.onLocationUpdate(ways);
    } // */
}
