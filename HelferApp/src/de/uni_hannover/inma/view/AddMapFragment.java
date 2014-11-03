package de.uni_hannover.inma.view;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import de.uni_hannover.inma.IDs;
import de.uni_hannover.inma.MainActivity;
import de.uni_hannover.inma.R;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Way;

public class AddMapFragment extends SupportMapFragment implements OnMapClickListener{

	public interface OnDataTransmitListener {
		public void onDataTransmit();
	}
	
	private OnDataTransmitListener mCallback;
	private List<Way> ways = null;
	private Coordinate location = null;
	private String tagname = null;
	private String tagid = null;
	private GoogleMap mMap = null;
	private Way newlyInsertet = null;
	private boolean edit = false;
	
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		Bundle intent = getArguments();
		ways = (List<Way>) intent.getSerializable(IDs.WAYS);
		location = (Coordinate) intent.getSerializable(IDs.LOCATION);
		tagname = intent.getString(IDs.TAGNAME);
		tagid = intent.getString(IDs.TAGID);
		if(intent.containsKey(IDs.EDIT))
			edit = intent.getBoolean(IDs.EDIT);
		if(intent.containsKey(IDs.NEW_WAY))
			newlyInsertet = (Way) intent.getSerializable(IDs.NEW_WAY);

		return super.onCreateView(inflater, container, savedInstanceState);
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
        intent.putBoolean(IDs.EDIT, edit);
        if(newlyInsertet!= null) 
        	intent.putSerializable(IDs.NEW_WAY, newlyInsertet);
    }

	@SuppressLint("NewApi")
	public void onStart() {
		super.onStart();
		getActivity().getActionBar().setTitle(tagname);
		getActivity().invalidateOptionsMenu();
		mMap = getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.setOnMapClickListener(this);
		redraw();
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude,location.longitude), 19));

	}
	
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		MenuItem paint = menu.findItem(R.id.action_edit_map);
		if(paint!=null)
			paint.setVisible(true);
		MenuItem submit = menu.findItem(R.id.action_send_data);
		if(submit!=null)
			submit.setVisible(true);
		MenuItem edit = menu.findItem(R.id.action_add_tag);
		if(edit!=null)
			edit.setVisible(false);
		MenuItem update = menu.findItem(R.id.action_request_update);
		if(update!=null)
			update.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id==R.id.action_edit_map) {
			onClick(item);
			return true;
		}
		if(id==R.id.action_send_data) {
			addTagToOsm();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressWarnings("unchecked")
	public void addTagToOsm() {
		if(newlyInsertet!=null && newlyInsertet.getPolyline().getPoints().size()>2) {
			List<Way> single = new LinkedList<Way>();
			single.add(newlyInsertet);
			new InformUsTask().execute(single);
		}
		else {
			new InformUsTask().execute(ways);
		}
	}
	
	public void newData(List<Way> data) {
		this.ways = data;
		redraw();
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
		String value = clicked.getValue(tagid);
		if(value==null) 
			clicked.alterTag(tagid, "no");
		else if (value.equalsIgnoreCase("no"))
			clicked.alterTag(tagid, "limited");
		else if (value.equalsIgnoreCase("limited"))
			clicked.alterTag(tagid, "yes");
		else if (value.equalsIgnoreCase("yes"))
			clicked.removeTag(tagid);
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
		po.strokeColor(w.getStrokeColor(tagid)).fillColor(w.getFillColor(tagid));
		for (Coordinate c : w.getPolyline().getPoints())
			po.add(toLatLon(c));
		mMap.addPolygon(po);
	}

	private static LatLng toLatLon(Coordinate c) {
		return new LatLng(c.latitude, c.longitude);
	}

	public void onClick(MenuItem v) {
		if(edit) {
			// cancel gedrückt
			v.setIcon(R.drawable.ic_action_edit);
			newlyInsertet = null;
		}
		else {
			// zeichnen gedrückt.
			v.setIcon(R.drawable.ic_action_cancel);
			newlyInsertet = new Way();
			new ChooseDialogFragment().show(getActivity().getSupportFragmentManager(), "chooser");
		}
		edit = !edit;
		redraw();
	}
	
	
	public void afterInforming(int status) {
    	if(status<0)
			Toast.makeText(getActivity(), getString(R.string.no_data_transmit), Toast.LENGTH_SHORT).show();
    	else if(status==0)
    			Toast.makeText(getActivity(), getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
    	else {
    		Toast.makeText(getActivity(), getString(R.string.data_transmit), Toast.LENGTH_SHORT).show();
    		mCallback.onDataTransmit();
    	}
	}
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mCallback = (OnDataTransmitListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDataTransmitListener");
		}
		
	}

	
	@SuppressLint("NewApi")
	public void onPause() {
		super.onPause();
		getActivity().getActionBar().setTitle(R.string.app_name);
	}

public class ChooseDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.choose_restriction)
	           .setItems(R.array.area_possibilities, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   switch(which) {
	            	   case 0:
		            	   newlyInsertet.alterTag(tagid, "no");
		            	   break;
	            	   case 1:
		            	   newlyInsertet.alterTag(tagid, "limited");
		            	   break;
	            	   case 2:
		            	   newlyInsertet.alterTag(tagid, "yes");
		            	   break;
	            	   }
	            	   
	           }
	    });
	    return builder.create();
	}
}
	
private class InformUsTask extends AsyncTask<List<Way>, Integer, Integer> {

	@Override
	protected Integer doInBackground(List<Way>... params) {

		int status = OSM.alterContent(params[0], location) ;
		
		((MainActivity) getActivity()).reOrder();
		return status;
	}
	
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Integer status) {
    	afterInforming(status);
    }
}
}
