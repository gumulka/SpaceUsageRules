package de.uni_hannover.inma;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class ShowMapActivity extends ActionBarActivity implements OnMapClickListener, OnClickListener{

	private List<Way> ways = null;
	private Coordinate location = null;
	private String tagname = null;
	private GoogleMap mMap = null;
	private Way newlyInsertet = null;
	private boolean edit = false;
	Button paint_button = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_map);
		if (savedInstanceState == null) {
		    Intent intent = getIntent();
		    ways = (List<Way>) intent.getSerializableExtra(IDs.WAYS);
		    location = (Coordinate) intent.getSerializableExtra(IDs.LOCATION);
		    tagname = intent.getStringExtra(IDs.TAGNAME);
		}
		else {
			ways = (List<Way>) savedInstanceState.getSerializable(IDs.WAYS);
			location = (Coordinate) savedInstanceState.getSerializable(IDs.LOCATION);
			tagname = savedInstanceState.getString(IDs.TAGNAME);
			edit = savedInstanceState.getBoolean(IDs.EDIT);
			newlyInsertet = (Way) savedInstanceState.getSerializable(IDs.NEW_WAY);
		}
	}

	
	public void alterTagName(String newTagName) {
		tagname = newTagName;
		TextView tv = (TextView) findViewById(R.id.add_tag_name);
		tv.setText(newTagName);
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putSerializable(IDs.WAYS, (Serializable) ways);
        outState.putSerializable(IDs.LOCATION, location);
        outState.putString(IDs.TAGNAME, tagname);
        outState.putBoolean(IDs.EDIT, edit);
        outState.putSerializable(IDs.NEW_WAY, newlyInsertet);
    }

	public void addTagToOsm(View view) {
		if(newlyInsertet!=null) {
			new InformUsTask().execute(newlyInsertet);
		}
		else {
			Way[] add = new Way[ways.size()];
			int i = 0;
			for(Way w : ways) {
				if("true".equals(w.getValue("sur:clicked")))
					add[i++] = w;
			}
			new InformUsTask().execute(add);
		}
	}



    
	public void onStart() {
		super.onStart();
		
		paint_button = (Button) findViewById(R.id.paint_button);
		paint_button.setOnClickListener(this);
		if(edit) 
			paint_button.setText(getString(R.string.cancel));

		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.setOnMapClickListener(this);
		redraw();
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude,location.longitude), 19));

		TextView tv = (TextView) findViewById(R.id.add_tag_name);
		tv.setText(tagname);
	}

	@Override
	public void onMapClick(LatLng l) {
		Coordinate c = new Coordinate(l.latitude, l.longitude);
		if (edit)
			addCoordinate(c);
		else
			markBuilding(c);
	}

	private void addCoordinate(Coordinate c) {
		if (newlyInsertet == null) {
			newlyInsertet = new Way();
			newlyInsertet.addTag("sur:tag", tagname);
		}
		newlyInsertet.addCoordinate(c);
		redraw();
	}

	private void markBuilding(Coordinate c) {
		Way clicked = null;
		for (Way w : ways) {
			if (c.inside(w.getCoordinates())) {
				clicked = w;
				if ("true".equalsIgnoreCase(w.getValue("sur:clicked")))
					w.addTag("sur:clicked", "false");
				else
					w.addTag("sur:clicked", "true");
			}
		}
		if (clicked == null)
			return;
		redraw();
	}

	private void redraw() {
		mMap.clear();
		if(edit) {
			updateMapPart(newlyInsertet);
		} else {
			for (Way w : ways)
				if(w.isArea())
					updateMapPart(w);
			MarkerOptions mo = new MarkerOptions();
			mo.title(getResources().getString(R.string.your_position));
			mo.position(new LatLng(location.latitude,location.longitude));
			mMap.addMarker(mo);
		}
	}

	private void updateMapPart(Way w) {
		if (!w.isValid())
			return;
		PolygonOptions po = new PolygonOptions();
		po.strokeWidth(2);
		po.strokeColor(w.getStrokeColor()).fillColor(w.getFillColor());
		for (Coordinate c : w.getCoordinates())
			po.add(toLatLon(c));
		mMap.addPolygon(po);
	}

	private static LatLng toLatLon(Coordinate c) {
		return new LatLng(c.latitude, c.longitude);
	}

	@Override
	public void onClick(View v) {
		if(edit) {
			// cancel gedrückt
			paint_button.setText(getString(R.string.paint));
		}
		else {
			// zeichnen gedrückt.
			paint_button.setText(getString(R.string.cancel));
		}
		newlyInsertet = null;
		edit = !edit;
		redraw();
	}
	
	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_map, menu);
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
	
	public void afterInforming(int status) {
    	if(status<0)
			Toast.makeText(this, getString(R.string.no_data_transmit), Toast.LENGTH_SHORT).show();
    	else
    		Toast.makeText(this, getString(R.string.data_transmit), Toast.LENGTH_SHORT).show();
	}

	
private class InformUsTask extends AsyncTask<Way, Integer, Integer> {

	@Override
	protected Integer doInBackground(Way... params) {
		for(Way w: params) {
			if(w==null)
				continue;
			Connection con = Jsoup.connect("http://www.sur.gummu.de/add.php").method(Method.POST);
			String coords = "";
			for(Coordinate c : w.getCoordinates()) {
				coords += c.toString() + ";";
			}
			con.data("coords", coords);
			con.data("tag", tagname);
			con.data("standort",location.toString());
			
			try {
				Response res = con.execute();
				System.err.println(res.body());
			} catch (IOException e) {
				return -1;
			}
		}
		return 1;
	}
	
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Integer status) {
    	afterInforming(status);
    }
}
}
