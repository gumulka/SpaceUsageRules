package de.uni_hannover.inma.view;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import de.uni_hannover.inma.R;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class AddTagMap extends Fragment implements OnMapClickListener, OnClickListener{

	private String name;
	private LatLng location;
	private List<Way> ways;
	GoogleMap mMap;
	private Way newlyInsertet;
	private boolean edit = false;
	Button paint_button;
	
	public Way getNewWay() {
		return newlyInsertet;
	}

	public AddTagMap(String name, LatLng location, List<Way> ways) {
		this.name = name;
		this.location = location;
		this.ways = ways;
		for(Way w : ways) 
			w.addTag("sur:tag", name);
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
			newlyInsertet.addTag("sur:tag", name);
		}
		newlyInsertet.addCoordinate(c);
		mMap.clear();
		updateMapPart(newlyInsertet);
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.add_tag_map, container, false);
		mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.setOnMapClickListener(this);

		TextView textView = (TextView) rootView.findViewById(R.id.add_tag_name);
		textView.setText(name);

		paint_button = (Button) rootView.findViewById(R.id.paint_button);
		paint_button.setOnClickListener(this);
		
		redraw();
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 19));
		return rootView;
	}

	private void redraw() {
		mMap.clear();
		for (Way w : ways)
			if(w.isArea())
				updateMapPart(w);
		MarkerOptions mo = new MarkerOptions();
		mo.title(getResources().getString(R.string.your_position));
		mo.position(location);
		mMap.addMarker(mo);
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
			paint_button.setText(getActivity().getString(R.string.paint));
			newlyInsertet = null;
			redraw();
		}
		else {
			// zeichnen gedrückt.
			paint_button.setText(getActivity().getString(R.string.cancel));
			newlyInsertet = null;
			mMap.clear();
		}
		edit = !edit;
	}

}
