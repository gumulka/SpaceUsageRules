package de.uni_hannover.inma.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import de.uni_hannover.inma.IDs;
import de.uni_hannover.inma.R;
import de.uni_hannover.inma.R.id;
import de.uni_hannover.inma.R.layout;
import de.uni_hannover.inma.R.string;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class ShowMapFragment extends Fragment implements OnMapClickListener, OnClickListener{

	private List<Way> ways = null;
	private Coordinate location = null;
	private String tagname = null;
	private GoogleMap mMap = null;
	private Way newlyInsertet = null;
	private boolean edit = false;
	Button paint_button = null;
	MapView mv = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_show_map, container, false);

		if (savedInstanceState == null) {
			Bundle intent = getArguments();
		    ways = (List<Way>) intent.getSerializable(IDs.WAYS);
		    location = (Coordinate) intent.getSerializable(IDs.LOCATION);
		    tagname = intent.getString(IDs.TAGNAME);
		}
		else {
			ways = (List<Way>) savedInstanceState.getSerializable(IDs.WAYS);
			location = (Coordinate) savedInstanceState.getSerializable(IDs.LOCATION);
			tagname = savedInstanceState.getString(IDs.TAGNAME);
			edit = savedInstanceState.getBoolean(IDs.EDIT);
			if(savedInstanceState.containsKey(IDs.NEW_WAY))
				newlyInsertet = (Way) savedInstanceState.getSerializable(IDs.NEW_WAY);
			savedInstanceState.remove(IDs.NEW_WAY);
			savedInstanceState.remove(IDs.WAYS);
			savedInstanceState.remove(IDs.LOCATION);
		}

		mv = (MapView) rootView.findViewById(R.id.mapView);
		mv.onCreate(savedInstanceState);
	    
//		mv.onResume();// needed to get the map to display immediately

	    try {
	        MapsInitializer.initialize(getActivity().getApplicationContext());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		
		return rootView;
	}

	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putSerializable(IDs.WAYS, (Serializable) ways);
        outState.putSerializable(IDs.LOCATION, location);
        outState.putString(IDs.TAGNAME, tagname);
        outState.putBoolean(IDs.EDIT, edit);
        if(newlyInsertet!= null) 
        	outState.putSerializable(IDs.NEW_WAY, newlyInsertet);
    }

	public void onStart() {
		super.onStart();
		
		paint_button = (Button) getActivity().findViewById(R.id.paint_button);
		paint_button.setOnClickListener(this);
		if(edit) 
			paint_button.setText(getString(R.string.cancel));
		MapView mv = (MapView) getActivity().findViewById(R.id.mapView);
		mMap = mv.getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.setOnMapClickListener(this);
		redraw();
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude,location.longitude), 19));

		TextView tv = (TextView) getActivity().findViewById(R.id.add_tag_name);
		tv.setText(tagname);
	}

	public void alterTagName(String newTagName) {
		tagname = newTagName;
		TextView tv = (TextView) getActivity().findViewById(R.id.add_tag_name);
		tv.setText(newTagName);
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
		}
		newlyInsertet.addCoordinate(c);
		redraw();
	}

	private void markBuilding(Coordinate c) {
		Way clicked = null;
		for (Way w : ways) {
			if (w.getPolyline().inside(c)) {
				if(clicked == null || clicked.getArea() > w.getArea())
					clicked = w;
			}
		}
		if (clicked == null)
			return;
		if ("true".equalsIgnoreCase(clicked.getValue("sur:clicked")))
			clicked.addTag("sur:clicked", "false");
		else
			clicked.addTag("sur:clicked", "true");

		redraw();
	}

	private void redraw() {
		mMap.clear();
		if(edit) {
			if(newlyInsertet!=null)
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
	
	
	public void afterInforming(int status) {
    	if(status<0)
			Toast.makeText(getActivity(), getString(R.string.no_data_transmit), Toast.LENGTH_SHORT).show();
    	else
    		Toast.makeText(getActivity(), getString(R.string.data_transmit), Toast.LENGTH_SHORT).show();
	}

	
	public void onDestroy() {
		super.onDestroy();
		mv.onDestroy();
	}
	public void onResume() {
		super.onPause();
		mv.onResume();
	}
	public void onPause() {
		super.onPause();
		mv.onPause();
	}

@Override
public void onLowMemory() {
    super.onLowMemory();
    mv.onLowMemory();
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
