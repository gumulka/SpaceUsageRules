package de.uni_hannover.inma;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import de.uni_hannover.inma.view.AddMapFragment;
import de.uni_hannover.inma.view.AddMapFragment.OnDataTransmitListener;
import de.uni_hannover.inma.view.AddTagListFragment;
import de.uni_hannover.inma.view.AddTagListFragment.OnAddTagSelectedListener;
import de.uni_hannover.inma.view.HelpFragment;
import de.uni_hannover.inma.view.ShowMapFragment;
import de.uni_hannover.inma.view.ShowMapFragment.SearchFromHereInterface;
import de.uni_hannover.inma.view.ShowTagListFragment;
import de.uni_hannover.inma.view.ShowTagListFragment.OnShowTagSelectedListener;
import de.uni_hannover.inma.view.StartFragment;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Tag;
import de.uni_hannover.spaceusagerules.core.Way;
import de.uni_hannover.spaceusagerules.io.OSM;

public class MainActivity extends ActionBarActivity implements OnShowTagSelectedListener, OnAddTagSelectedListener, OnDataTransmitListener, SearchFromHereInterface {

	private Coordinate location = null;
	private Set<Way> ways = null;
	private List<Tag> umgebung = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState != null) {
			location = (Coordinate) savedInstanceState
					.getSerializable(IDs.LOCATION);
			ways = (Set<Way>) savedInstanceState.getSerializable(IDs.WAYS);
		} else {
			ways = new TreeSet<Way>();
			updateLocation();
			Fragment newFragment = new StartFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, newFragment).commit();		}
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
		outState.putSerializable(IDs.LOCATION, (Serializable) location);
		outState.putSerializable(IDs.WAYS, (Serializable) ways);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean prefOnly = sharedPref.getBoolean("pref_only", false);
		String prefOnly_string = sharedPref.getString("pref_only_tag", null);
		if(prefOnly && prefOnly_string!=null) {
			MenuItem list = menu.findItem(R.id.action_show_list);
			if(list!=null)
				list.setVisible(false);
		}
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
			Fragment newFragment = new HelpFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, newFragment).addToBackStack(null).commit();
			return true;
		}
		if(id == R.id.action_show_list) {
			if(location!=null && umgebung != null) {
				if(umgebung.size()>1)
					onDataTransmit();
				else
					Toast.makeText(this, R.string.no_alternative, Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(this, R.string.no_location, Toast.LENGTH_LONG).show();
			return true;
		}
		if(id == R.id.action_add_tag) {
			if(location!=null)
				addTag();
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
	
	@Override
	public void searchFromHere(LatLng l) {
		this.location = new Coordinate(l.latitude, l.longitude);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new QueryOsmTask().execute(false);
			Toast.makeText(getApplicationContext(), getString(R.string.start_search),
					Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(getApplicationContext(), getString(R.string.no_network),
					Toast.LENGTH_LONG).show();	}
	
	private void addTag() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean prefOnly = sharedPref.getBoolean("pref_only", false);
		String prefOnly_string = sharedPref.getString("pref_only_tag", null);
		if(prefOnly && prefOnly_string!=null) {
    		new QueryOsmTask().execute(true);
	        Resources res = getResources();
	        String[] tagList = res.getStringArray(R.array.tags);
	        int i = 0;
	        for(String s : tagList) {
	        	if(s.equals(prefOnly_string)) {
	        		onAddTagSelected(i);
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
				.add(R.id.container, newFragment).commit();
	}

	public void onNewDataAvailable() {
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
			System.err.println("KEKSE");
			((AddMapFragment) f).newData(ways);
			return;
		}
		if(f instanceof AddTagListFragment)
			return ;
		if(f instanceof ShowTagListFragment) {
			
			return;
		}
		if(f instanceof StartFragment) {
			if (umgebung.isEmpty()) {
				Tag t = new Tag("","");
				onShowTagSelected(t);
			} else if(umgebung.size()==1) {
				onShowTagSelected(umgebung.get(0));
			}
			else {
				onDataTransmit();
			}
			return;
		}
		System.err.println("Ich sollte niemals hierher kommen!!!!!");
		System.err.println(f);
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
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment);
		if(umgebung.size()>1)
			trans = trans.addToBackStack(null);
		trans.commit();
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
		return (float) (0.0001 + 0.003 * syncConnPref*syncConnPref);
	}
	
	private class LocationUpdateListener implements LocationListener {
		private Location last_location = null;
		private boolean location_available = false;

		public void onLocationChanged(Location loc) {
			if(last_location==null || last_location.distanceTo(loc)>20) {
			location = new Coordinate(loc.getLatitude(),loc.getLongitude());
			last_location = loc;
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				new QueryOsmTask().execute(false);
				Toast.makeText(getApplicationContext(), getString(R.string.start_search),
						Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(getApplicationContext(), getString(R.string.no_network),
						Toast.LENGTH_LONG).show();
		}
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

	public void order(Set<Way> ways) {
		this.ways.addAll(ways);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean prefOnly = sharedPref.getBoolean("pref_only", false);
		String prefOnly_string = sharedPref.getString("pref_only_tag", null);

		Resources res = getResources();
        String[] tagList = res.getStringArray(R.array.tags);
        String[] readableList = res.getStringArray(R.array.tags_readable);
        
		Map<String, Tag> possible = new TreeMap<String, Tag>();
		if(umgebung != null) {
			for(Tag t : umgebung) {
				possible.put(t.getTagId(), t);
			}
			umgebung.clear();
		}else
			umgebung = new LinkedList<Tag>();
		if(prefOnly && prefOnly_string != null) {
			if(!possible.containsKey(prefOnly_string))
				possible.put(prefOnly_string,new Tag(prefOnly_string,prefOnly_string));
		} else {
			for(int i = 0;i<tagList.length;i++)
				if(!possible.containsKey(tagList[i]))
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
			if (!t.isEmpty() || possible.size() <2)
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
			boolean pref_only = sharedPref.getBoolean("pref_only", false);
			String prefOnly_string = sharedPref.getString("pref_only_tag", null);
			Set<Way> ways = null;
			if(loc[0]) {
				ways = OSM.getObjectList(location, getRadius());
			}
			else if(pref_only && prefOnly_string != null) {
				ways = OSM.getObjectList(location, getRadius()*2, prefOnly_string);
			} else {
				ways = OSM.getObjectList(location, getRadius());
			}
			order(ways);
			return 1;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Integer a) {
			onNewDataAvailable();
		}
	}
}
