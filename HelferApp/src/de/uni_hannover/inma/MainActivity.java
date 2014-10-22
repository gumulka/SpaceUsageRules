package de.uni_hannover.inma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import de.uni_hannover.inma.view.AddMapFragment;
import de.uni_hannover.inma.view.AddTagListFragment;
import de.uni_hannover.inma.view.AddTagListFragment.OnAddTagSelectedListener;
import de.uni_hannover.inma.view.PlaceholderFragment;
import de.uni_hannover.inma.view.ShowMapFragment;
import de.uni_hannover.inma.view.ShowTagListFragment;
import de.uni_hannover.inma.view.ShowTagListFragment.OnShowTagSelectedListener;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Tag;
import de.uni_hannover.spaceusagerules.core.Way;

public class MainActivity extends ActionBarActivity implements OnShowTagSelectedListener, OnAddTagSelectedListener{

	private int layoutID = R.layout.fragment_main;
	private Coordinate location = null;
	private List<Way> ways = null;
	private float area = (float) 0.0005;
	private LinkedList<String> possibilities = null;
	private boolean dirty = false;
	private List<Tag> umgebung;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (location == null) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			LocationUpdateListener locationListener = new LocationUpdateListener();

			// Register the listener with the Location Manager to receive
			// location updates
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 5, 50, locationListener);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000, 50, locationListener);
		}

		if (savedInstanceState != null) {
			layoutID = savedInstanceState.getInt(IDs.LAYOUT_ID);
			location = (Coordinate) savedInstanceState
					.getSerializable(IDs.LOCATION);
			area = savedInstanceState.getFloat(IDs.AREA, (float) 0.0005);
			ways = (List<Way>) savedInstanceState.getSerializable(IDs.WAYS);
			dirty = savedInstanceState.getBoolean(IDs.DIRTY, false);
		}
		replaceFragment();
	}

	public void setDirty(boolean dirty) {
		System.err.println("Dirty is now " + dirty);
		this.dirty = dirty;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putInt(IDs.LAYOUT_ID, layoutID);
		outState.putSerializable(IDs.LOCATION, (Serializable) location);
		outState.putSerializable(IDs.WAYS, (Serializable) ways);
		outState.putBoolean(IDs.DIRTY, dirty);
	}

	private void replaceFragment() {
		Fragment newFragment = new PlaceholderFragment();
		Bundle args = new Bundle();
		args.putInt(IDs.LAYOUT_ID, layoutID);
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).commit();
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
		if(id == R.id.action_add_tag) {
			Fragment newFragment = new AddTagListFragment();
			Bundle args = new Bundle();
		    args.putSerializable(IDs.LOCATION, location);
		    args.putSerializable(IDs.WAYS, (Serializable) ways);
		    args.putSerializable(IDs.POSSIBILITIES, getPossibilities());
			newFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, newFragment).addToBackStack(null).commit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateLocation(Location location) {
		if(dirty)
			return;
		Coordinate l = new Coordinate(location.getLatitude(),
				location.getLongitude());
		this.location = l;
		this.area = (float) 0.0005;
		updateOsmData();
	}
	
	public void increase(View view) {
		this.area *=2;
		updateOsmData();
	}
	public void addTag(View view) {
		Fragment newFragment = new AddTagListFragment();
		Bundle args = new Bundle();
	    args.putSerializable(IDs.LOCATION, location);
	    args.putSerializable(IDs.WAYS, (Serializable) ways);
	    args.putSerializable(IDs.POSSIBILITIES, getPossibilities());
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
	}

	public void onLocationUpdate() {
		if(dirty)
			return;
		if (umgebung.isEmpty()) {
			layoutID = R.layout.help_us;
			replaceFragment();
		} else {
			Fragment newFragment = new ShowTagListFragment();
			Bundle args = new Bundle();
		    args.putSerializable(IDs.LOCATION, location);
		    args.putSerializable(IDs.TAGS, (Serializable) umgebung);
		    newFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, newFragment).addToBackStack(null).commit();
			setDirty(true);
		}
	}

	private void updateOsmData() {
		// Called when a new location is found by the network location provider.
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new QueryOsmTask().execute(location);
			Toast.makeText(this, getString(R.string.start_search),
					Toast.LENGTH_SHORT).show();
		} // */
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
	    args.putSerializable(IDs.TAGNAME, t.toString());
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
	}

	@Override
	public void onAddTagSelected(String tagname) {
		Fragment newFragment = new AddMapFragment();
		Bundle args = new Bundle();
	    args.putSerializable(IDs.LOCATION, location);
	    args.putSerializable(IDs.WAYS, (Serializable) ways);
	    args.putSerializable(IDs.TAGNAME, tagname);
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
	}

	public float getRadius() {
		return area;
	}

	public LinkedList<String> getPossibilities() {
		if (possibilities == null) {
			possibilities = new LinkedList<String>();
			AssetManager assetManager = getAssets();
			try {
				InputStream ins = assetManager.open("possibilities.txt");
				BufferedReader r = new BufferedReader(
						new InputStreamReader(ins));
				String line;
				while ((line = r.readLine()) != null) {
					possibilities.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return possibilities;
	}

	private class LocationUpdateListener implements LocationListener {

		private boolean location_available = false;

		public void onLocationChanged(Location location) {
			updateLocation(location);
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

		Map<String, Tag> possibilities = new TreeMap<String, Tag>();
		umgebung = new LinkedList<Tag>();
		for (String line : getPossibilities())
			possibilities.put(line, new Tag(line));
		for (Way w : ways) {
			for (String s : w.getTags().keySet()) {
				if (possibilities.keySet().contains(s)) {
					Tag t = possibilities.get(s);
					if (t != null)
						t.addWay(w);
				}
			}
		}
		for (Tag t : possibilities.values()) {
			if (!t.isEmpty())
				umgebung.add(t);
		}
	}
	
	public void reOrder() {
		order(ways);
	}
	
	public class QueryOsmTask extends AsyncTask<Coordinate, Integer, Integer> {

		@Override
		protected Integer doInBackground(Coordinate... urls) {
			List<Way> ways = OSM.getObjectList(urls[0], getRadius());
			order(ways);
			return 1;

		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Integer a) {
			onLocationUpdate();
		} // */
	}

}
