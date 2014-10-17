package de.uni_hannover.spaceusagerules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Image;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.fragments.MapHandler;
import de.uni_hannover.spaceusagerules.gen_alg.Population;

/**
 * Created by gumulka on 10/10/14.
 */
public class CupUpdateListener extends AsyncTask<Coordinate, Void, List<Way>> {

    private boolean obsolete = false;
    Coordinate location;
    int position;
    File file;

    public CupUpdateListener(int position) {
    	this.position = position;
        // Called when a new location is found by the network location provider.
        ConnectivityManager connMgr = (ConnectivityManager)
                MainActivity.context().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

       	try {
    		AssetManager assetManager = MainActivity.context().getAssets();
    		String filename = String.format(Locale.GERMAN, "%04d.jpg",position);
    		InputStream ins = assetManager.open(filename);
    		location = Image.readCoordinates(ins);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		String filename = String.format(Locale.GERMAN, "%02.4f_%02.4f.xml",location.latitude, location.longitude);
		file = new File(MainActivity.context().getFilesDir(), filename);

		if (networkInfo != null && networkInfo.isConnected() || (file.exists() && file.canRead())) {
    		execute(location);
        }
		else
			Toast.makeText(MainActivity.context(), "Kein Netzwerk verfügbar", Toast.LENGTH_LONG).show();
    }
	
	private Map<String,Double> readWeights(String attribute)  {
        String filename = "Rules.txt";
        Map<String,Double> blub = new TreeMap<String,Double>();
        AssetManager assetManager = MainActivity.context().getAssets();
        try {
            InputStream ins = assetManager.open(filename);

            BufferedReader r = new BufferedReader(new InputStreamReader(ins));
            String line;
            r.readLine();
            while ((line = r.readLine()) != null) {
            	String s = line.substring(0, line.indexOf('-')).trim();
            	if(!s.equals(attribute))
            		continue;
            	String bla = line.substring(line.indexOf('[')+1);
            	bla = bla.substring(0, bla.indexOf(']')).trim();
            	if(bla.length()==0)
            		break;
            	for(String part : bla.split(",")) {
            		String key = part.substring(0, part.indexOf("->"));
            		String d = part.substring(part.indexOf("->")+2);
            		double value = Double.parseDouble(d);
            		blub.put(key, value);
            	}
            	break;
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
		return blub;
	}
	
	private List<String> readAttributes(int postion) {
		List<String> attributes = new LinkedList<String>();
        String filename = "Data.txt";
        AssetManager assetManager = MainActivity.context().getAssets();
        try {
            InputStream ins = assetManager.open(filename);

            BufferedReader r = new BufferedReader(new InputStreamReader(ins));
            String line;
            r.readLine();
            while ((line = r.readLine()) != null) {
            	String[] s = line.split(",");
            	if(Integer.parseInt(s[0]) == position){
            		attributes.add(s[3].trim());
            	}
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
        return attributes;
	}
	
    protected List<Way> doInBackground(Coordinate... urls) {
    	Coordinate l = urls[0];
    	Map<String,Double> weights = new TreeMap<String,Double>();
    	for(String s : readAttributes(position)) {
    		for(Entry<String, Double> e : readWeights(s).entrySet()) {
    			if(weights.containsKey(e.getKey())) {
    				weights.put(e.getKey(), (weights.get(e.getKey()) + e.getValue())/2); 
    			}
    		}
    	}
    	List<Way> ways = OSM.getObjectList(l, file); //,(float) 0.0015);
    	Way best = Population.getNearestArea(l,ways,weights);
    	best.addTag("InformatiCup", "guess");
    	return ways;
    }

    public void makeObsolete() {
        this.obsolete = true;
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(List<Way> ways) {
        if(obsolete)
            return;
        MapHandler res = (MapHandler) ((MainActivity) MainActivity.context()).getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        res.addOSMData(ways);
    }
}
