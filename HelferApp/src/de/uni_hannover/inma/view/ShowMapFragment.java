package de.uni_hannover.inma.view;

import java.io.Serializable;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import de.uni_hannover.inma.IDs;
import de.uni_hannover.inma.R;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class ShowMapFragment extends SupportMapFragment implements OnMapLongClickListener{

	private Set<Way> ways = null;
	private Coordinate location = null;
	private String tagname = null;
	private String tagid = null;
	private GoogleMap mMap = null;
	private SearchFromHereInterface mCallback;
	
	public interface SearchFromHereInterface {
		public void searchFromHere(LatLng l);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		Bundle intent = getArguments();
		ways = (Set<Way>) intent.getSerializable(IDs.WAYS);
		location = (Coordinate) intent.getSerializable(IDs.LOCATION);
		tagname = intent.getString(IDs.TAGNAME);
		tagid = intent.getString(IDs.TAGID);

		mMap = getMap();
		mMap.setOnMapLongClickListener(this);
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		redraw();
		if(savedInstanceState==null) {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude,location.longitude), 19));
		}
		return rootView;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle intent = getArguments();
        if(intent == null) {
        	intent = new Bundle();
        	setArguments(intent);
        }
        // Save the current article selection in case we need to recreate the fragment
        intent.putSerializable(IDs.WAYS, (Serializable) ways);
        intent.putSerializable(IDs.LOCATION, location);
        intent.putString(IDs.TAGNAME, tagname);
        intent.putString(IDs.TAGID, tagid);
    }

	public void onStart() {
		super.onStart();
		getActivity().getActionBar().setTitle(tagname);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (SearchFromHereInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement SearchFromHereInterface");
		}
	}

	public String getTagID() {
		return tagid;
	}
	
	public void newData(Set<Way> ways) {
		this.ways.addAll(ways);
		redraw();
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
		po.strokeColor(w.getStrokeColor(tagid)).fillColor(w.getFillColor(tagid));
		for (Coordinate c : w.getPolyline().getPoints())
			po.add(toLatLon(c));
		mMap.addPolygon(po);
	}

	private static LatLng toLatLon(Coordinate c) {
		return new LatLng(c.latitude, c.longitude);
	}

	public void onPause() {
		super.onPause();
		getActivity().getActionBar().setTitle(R.string.app_name);
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		new SearchDialogFragment(arg0).show(getFragmentManager(), "askuser");
	}
	
	public class SearchDialogFragment extends DialogFragment {
		
		private LatLng l;
		
		public SearchDialogFragment(LatLng l) {
			this.l = l;
		}
		
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(R.string.dialog_search_here)
	               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   mCallback.searchFromHere(l);
	                   }
	               })
	               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // User cancelled the dialog
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
}
