package de.uni_hannover.inma.view;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import de.uni_hannover.inma.R;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class AddTagMap extends Fragment  implements OnMapClickListener {

	private String name;
	private LatLng location;
	private List<Way> ways;
	GoogleMap mMap;
	
	public AddTagMap(String name, LatLng location, List<Way> ways) {
		this.name = name;
		this.location = location;
		this.ways = ways;
	}
	
	@Override
	public void onMapClick(LatLng l) {
		Way clicked = null;
		Coordinate c = new Coordinate(l.latitude, l.longitude);
		for(Way w : ways) {
			w.addTag("sur:clicked", "false");
			if(c.inside(w.getCoordinates())) {
				clicked = w;
				w.addTag("sur:clicked", "true");
			}
		}
		if(clicked == null)
			return;
		redraw();

		MarkerOptions mo = new MarkerOptions();
//        mo.title(getResources().getString(R.string.your_position));
        mo.position(l);
        mMap.addMarker(mo);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.add_tag_map, container,
				false);
		mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMapClickListener(this);

		TextView textView = (TextView) rootView.findViewById(R.id.add_tag_name);
		textView.setText(name);
		        
		redraw();
		
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
		return rootView;
	}
	
	private void redraw() {
        mMap.clear();
		for(Way w : ways) 
			updateMapPart(w);

		MarkerOptions mo = new MarkerOptions();
        mo.title(getResources().getString(R.string.your_position));
        mo.position(location);
        mMap.addMarker(mo);

	}
	
    private void updateMapPart(Way w) {
        if(!w.isValid())
            return;
        if(w.isArea()) {
            PolygonOptions po = new PolygonOptions();
            po.strokeWidth(2);
            po.strokeColor(w.getStrokeColor()).fillColor(w.getFillColor());
            for (Coordinate c : w.getCoordinates())
                po.add(toLatLon(c));
            mMap.addPolygon(po);
        }
        else {
            PolylineOptions po = new PolylineOptions();
            po.color(w.getStrokeColor());
            po.width(5);
            for (Coordinate c : w.getCoordinates())
                po.add(toLatLon(c));
            mMap.addPolyline(po);
        }
    }
	
    private static LatLng toLatLon(Coordinate c) {
        return new LatLng(c.latitude,c.longitude);
    }
    
}
