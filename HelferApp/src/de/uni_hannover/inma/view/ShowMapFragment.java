package de.uni_hannover.inma.view;

import java.io.Serializable;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import de.uni_hannover.inma.IDs;
import de.uni_hannover.inma.R;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class ShowMapFragment extends Fragment{

	private List<Way> ways = null;
	private Coordinate location = null;
	private String tagname = null;
	private GoogleMap mMap = null;
	private MapView mv = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_show_map, container, false);

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
    }

	@SuppressLint("NewApi")
	public void onStart() {
		super.onStart();
		MapView mv = (MapView) getActivity().findViewById(R.id.mapView);
		mMap = mv.getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setZoomControlsEnabled(false);
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
	

	private void redraw() {
		mMap.clear();
		for (Way w : ways)
			if(w.isArea())
				updateMapPart(w);
		MarkerOptions mo = new MarkerOptions();
		mo.title(getResources().getString(R.string.your_position));
		mo.position(new LatLng(location.latitude,location.longitude));
		mMap.addMarker(mo);
	}

	private void updateMapPart(Way w) {
		if (!w.isValid())
			return;
		PolygonOptions po = new PolygonOptions();
		po.strokeWidth(2);
		po.strokeColor(w.getStrokeColor(tagname)).fillColor(w.getFillColor(tagname));
		for (Coordinate c : w.getCoordinates())
			po.add(toLatLon(c));
		mMap.addPolygon(po);
	}

	private static LatLng toLatLon(Coordinate c) {
		return new LatLng(c.latitude, c.longitude);
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
	
}
