package de.uni_hannover.inma.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import de.uni_hannover.inma.R;
import de.uni_hannover.inma.controller.InformUsTask;
import de.uni_hannover.inma.controller.LocationUpdateListener;
import de.uni_hannover.inma.controller.QueryOsmTask;
import de.uni_hannover.inma.view.AddTagFragment;
import de.uni_hannover.inma.view.AddTagMap;
import de.uni_hannover.inma.view.PlaceholderFragment;
import de.uni_hannover.inma.view.Umgebung;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class MainActivity extends ActionBarActivity {

	private Location location;
	private float area;
	private List<Way> ways;

    private List<String> possibilities;
    private AddTagMap tagMap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationUpdateListener((MainActivity) this);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(R.layout.main_fragment)).commit();
		}
		
		possibilities = new LinkedList<String>();
		AssetManager assetManager = getAssets();
        try {
            InputStream ins = assetManager.open("possibilities.txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(ins));
            String line;
            while ((line = r.readLine()) != null) {
            	possibilities.add(line);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
	}

	public List<String> getPossibilities() {
		return possibilities;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void addTag(View view) {
		if(possibilities!=null)
		getSupportFragmentManager().beginTransaction()
		.add(R.id.container, new AddTagFragment(possibilities))
        .addToBackStack(null).commit(); // */
	}
	
	public void increase(View view) {
		this.area *= 2;
		updateOsmData();
	}
	
	public void addAreaData(List<Way> ways) {
		this.ways = ways;
	}

	public void onLocationUpdate(List<Tag> umgebung) {
		if(umgebung.isEmpty())
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment(R.layout.help_us)).commit();
		else
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new Umgebung(umgebung)).commit();
	}
	
	public void showMap(Tag t) {
		
	}
	
	public void addTagToOsm(View v) {
		Way wa = tagMap.getNewWay();
		if(wa!=null) {
			new InformUsTask(this, new Coordinate(location.getLatitude(),location.getLongitude())).execute(wa);
		}
		else {
			Way[] add = new Way[ways.size()];
			int i = 0;
			for(Way w : ways) {
				if("true".equals(w.getValue("sur:clicked")))
					add[i++] = w;
			}
			new InformUsTask(this, new Coordinate(location.getLatitude(),location.getLongitude())).execute(add);
		}
	}
	
	public void addTagToMap(String tagname) {
		LatLng l = new LatLng(location.getLatitude(),location.getLongitude());
		tagMap = new AddTagMap(tagname,l,ways);
		getSupportFragmentManager().beginTransaction()
		.add(R.id.container, tagMap).addToBackStack(null).commit();
	}
	
	public void updateLocation(Location l) {
		this.location = l;
		this.area = (float) 0.0005;
		updateOsmData();
	}
	
	private void updateOsmData() {
        // Called when a new location is found by the network location provider.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new QueryOsmTask(this, area).execute(location);
			Toast.makeText(this, getString(R.string.start_search), Toast.LENGTH_SHORT).show();
        } // */
		else
			Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show();		
	}
	
}
