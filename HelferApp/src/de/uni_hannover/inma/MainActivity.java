package de.uni_hannover.inma;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import de.uni_hannover.inma.view.AddMapFragment;
import de.uni_hannover.inma.view.AddMapFragment.OnDataTransmitListener;
import de.uni_hannover.inma.view.AddTagListFragment;
import de.uni_hannover.inma.view.AddTagListFragment.OnAddTagSelectedListener;
import de.uni_hannover.inma.view.PlaceholderFragment;
import de.uni_hannover.inma.view.ShowMapFragment;
import de.uni_hannover.inma.view.ShowMapFragment.SearchFromHereInterface;
import de.uni_hannover.inma.view.ShowTagListFragment;
import de.uni_hannover.inma.view.ShowTagListFragment.OnShowTagSelectedListener;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Tag;
import de.uni_hannover.spaceusagerules.core.Way;

public class MainActivity extends ActionBarActivity implements OnShowTagSelectedListener, OnAddTagSelectedListener, OnDataTransmitListener, SearchFromHereInterface{

	private int layoutID = R.layout.fragment_main;
	private Coordinate location = null;
	private List<Way> ways = null;
	private float area = (float) 0.005;
	private List<Tag> umgebung = null;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState != null) {
			layoutID = savedInstanceState.getInt(IDs.LAYOUT_ID);
			location = (Coordinate) savedInstanceState
					.getSerializable(IDs.LOCATION);
			area = savedInstanceState.getFloat(IDs.AREA, (float) 0.005);
			ways = (List<Way>) savedInstanceState.getSerializable(IDs.WAYS);
		} else {
			updateLocation();
			replaceFragment();
		}
	}

	private void updateLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		LocationUpdateListener locationListener = new LocationUpdateListener();

		// Register the listener with the Location Manager to receive
		// location updates
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener,null);
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener,null);
//		locationManager.requestLocationUpdates(
//				LocationManager.GPS_PROVIDER, 5000, 50, locationListener);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putInt(IDs.LAYOUT_ID, layoutID);
		outState.putSerializable(IDs.LOCATION, (Serializable) location);
		outState.putSerializable(IDs.WAYS, (Serializable) ways);
	}

	private void replaceFragment() {
		Fragment newFragment = new PlaceholderFragment();
		Bundle args = new Bundle();
		args.putInt(IDs.LAYOUT_ID, layoutID);
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
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
		    Intent intent = new Intent(this, SettingsActivity.class);
		    startActivity(intent);
			return true;
		}
		if(id == R.id.action_help) {
			layoutID = R.layout.first_use;
			replaceFragment();
			return true;
		}
		if(id == R.id.action_add_tag) {
			if(location!=null)
				addTag(null);
			else
				Toast.makeText(this, R.string.no_location, Toast.LENGTH_LONG).show();
			return true;
		}
		if(id==R.id.action_request_update){
			updateLocation();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateLocation(Location location) {
		this.location = new Coordinate(location.getLatitude(),
				location.getLongitude());
		updateOsmData();
	}
	
	@Override
	public void searchFromHere(LatLng l) {
		this.location = new Coordinate(l.latitude, l.longitude);
		updateOsmData();
	}
	
	public void increase(View view) {
		this.area *=2;
		updateOsmData();
	}
	
	public void showMap(View view) {
		Tag t = new Tag("","");
		onShowTagSelected(t);
	}
	
	public void addTag(View view) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean prefOnly = sharedPref.getBoolean("pref_only", false);
		String prefOnly_string = sharedPref.getString("pref_only_tag", null);
		if(prefOnly && prefOnly_string!=null) {
	        Resources res = getResources();
	        String[] tagList = res.getStringArray(R.array.tags);
	        int i = 0;
	        for(String s : tagList) {
	        	if(s.equals(prefOnly_string)) {
	        		onAddTagSelected(i);
	        		new QueryOsmTask().execute(false);
	        		return;
	        	}
	        	i++;
	        }
		} else {
			Fragment newFragment = new AddTagListFragment();
			Bundle args = new Bundle();
			args.putSerializable(IDs.LOCATION, location);
			args.putSerializable(IDs.WAYS, (Serializable) ways);
			newFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, newFragment).addToBackStack(null).commit();
		}
	}

	@Override
	public void onDataTransmit() {
		Fragment newFragment = new ShowTagListFragment();
		Bundle args = new Bundle();
	    args.putSerializable(IDs.LOCATION, location);
	    args.putSerializable(IDs.TAGS, (Serializable) umgebung);
	    newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
	}

	public void onLocationUpdate() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
		if(f instanceof ShowMapFragment) {
			String t = ((ShowMapFragment) f).getTagID();
			if(t.length()>1) {
				for(Tag tag : umgebung)
					if(tag.getTagId().equals(t)) {
						((ShowMapFragment) f).newData(tag.getWays());
						return;
					}
				Toast.makeText(this, R.string.no_data_found, Toast.LENGTH_LONG).show();
				return;
			}
		}
		if(f instanceof AddMapFragment) {
			((AddMapFragment) f).newData(ways);
			return;
		}
		if (umgebung.isEmpty()) {
			layoutID = R.layout.help_us;
			replaceFragment();
		} else if(umgebung.size()==1) {
			onShowTagSelected(umgebung.get(0));
		}
		else {
			onDataTransmit();
		}
	}

	private void updateOsmData() {
		// Called when a new location is found by the network location provider.
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			boolean prefOnly = sharedPref.getBoolean("pref_only", false);
			String prefOnly_string = sharedPref.getString("pref_only_tag", null);
			new QueryOsmTask().execute(prefOnly && prefOnly_string != null);
			Toast.makeText(this, getString(R.string.start_search),
					Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(this, getString(R.string.no_network),
					Toast.LENGTH_LONG).show();
	}

	@Override
	public void onShowTagSelected(Tag t) {
		Fragment newFragment = new ShowMapFragment();
		Bundle args = new Bundle();
	    args.putSerializable(IDs.LOCATION, location);
	    args.putSerializable(IDs.WAYS, (Serializable) t.getWays());
	    args.putSerializable(IDs.TAGNAME, t.getName());
	    args.putSerializable(IDs.TAGID, t.getTagId());
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
	}

	@Override
	public void onAddTagSelected(int tagposition) {

        Resources res = getResources();
        String[] tagList = res.getStringArray(R.array.tags);
        String[] readableList = res.getStringArray(R.array.tags_readable);
        
		Fragment newFragment = new AddMapFragment();
		Bundle args = new Bundle();
	    args.putSerializable(IDs.LOCATION, location);
	    args.putSerializable(IDs.WAYS, (Serializable) ways);
	    args.putSerializable(IDs.TAGNAME, readableList[tagposition]);
	    args.putSerializable(IDs.TAGID, tagList[tagposition]);
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
	}

	public float getRadius() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		float syncConnPref = sharedPref.getFloat("search_range", (float) 0.5);
		return (float) (0.0001 + area * syncConnPref*syncConnPref);
	}
	
	private class LocationUpdateListener implements LocationListener {

		private boolean location_available = false;
		private boolean updated = false;

		public void onLocationChanged(Location location) {
			if(!updated) {
				updateLocation(location);
			}
			updated = true;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
			if (!location_available)
				 Toast.makeText(getApplicationContext(), getString(R.string.found_location), Toast.LENGTH_SHORT).show();
			location_available = true;
		}

		public void onProviderDisabled(String provider) {
			location_available = false;
			Toast.makeText(getApplicationContext(), getString(R.string.no_location),Toast.LENGTH_LONG).show();
		}

	}

	public void order(List<Way> ways) {
		this.ways = ways;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean prefOnly = sharedPref.getBoolean("pref_only", false);
		String prefOnly_string = sharedPref.getString("pref_only_tag", null);

		Resources res = getResources();
        String[] tagList = res.getStringArray(R.array.tags);
        String[] readableList = res.getStringArray(R.array.tags_readable);
        
		Map<String, Tag> possible = new TreeMap<String, Tag>();
		umgebung = new LinkedList<Tag>();
		if(prefOnly && prefOnly_string != null) {
			possible.put(prefOnly_string,new Tag(prefOnly_string,prefOnly_string));
		} else {
			for(int i = 0;i<tagList.length;i++)
				possible.put(tagList[i], new Tag(readableList[i],tagList[i]));
		}
		for (Way w : ways) {
			for (String s : w.getTags().keySet()) {
				if (possible.keySet().contains(s)) {
					Tag t = possible.get(s);
					if (t != null)
						t.addWay(w);
				}
			}
		}
		for (Tag t : possible.values()) {
			if (!t.isEmpty())
				umgebung.add(t);
		}
	}
	
	public void reOrder() {
		order(ways);
	}
	
	public class QueryOsmTask extends AsyncTask<Boolean, Integer, Integer> {

		@Override
		protected Integer doInBackground(Boolean... loc) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String prefOnly_string = sharedPref.getString("pref_only_tag", null);
			List<Way> ways = null;
			if(loc[0] && prefOnly_string != null) {
				ways = OSM.getObjectList(location, getRadius()*2, prefOnly_string);
			} else
				ways = OSM.getObjectList(location, getRadius());
			order(ways);
			return 1;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Integer a) {
			onLocationUpdate();
		}
	}
}
