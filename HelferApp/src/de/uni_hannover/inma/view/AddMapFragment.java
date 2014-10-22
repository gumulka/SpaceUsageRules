package de.uni_hannover.inma.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import de.uni_hannover.inma.IDs;
import de.uni_hannover.inma.MainActivity;
import de.uni_hannover.inma.R;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class AddMapFragment extends Fragment implements OnMapClickListener{

	private List<Way> ways = null;
	private Coordinate location = null;
	private String tagname = null;
	private GoogleMap mMap = null;
	private Way newlyInsertet = null;
	private boolean edit = false;
	private MapView mv = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_add_map, container, false);

		setHasOptionsMenu(true);
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

	@SuppressLint("NewApi")
	public void onStart() {
		super.onStart();
		getActivity().invalidateOptionsMenu();
		MapView mv = (MapView) getActivity().findViewById(R.id.mapView);
		mMap = mv.getMap();
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
	

	public void alterTagName(String newTagName) {
		tagname = newTagName;
	}
	
	public void addTagToOsm() {
		if(newlyInsertet!=null && newlyInsertet.getCoordinates().size()>2) {
			new InformUsTask().execute(newlyInsertet);
		}
		else {
			Way[] add = new Way[ways.size()];
			int i = 0;
			for(Way w : ways) {
				if("true".equals(w.getValue("sur:clicked")))
					add[i++] = w;
			}
			if(i!=0)
				new InformUsTask().execute(add);
			else {
				Toast.makeText(getActivity(), getString(R.string.nothing_selected), Toast.LENGTH_LONG).show();
			}
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
		clicked.addTag("sur:clicked", "true");
		String value = clicked.getValue(tagname);
		if(value==null) 
			clicked.addTag(tagname, "no");
		else if (value.equalsIgnoreCase("no"))
			clicked.addTag(tagname, "partly");
		else if (value.equalsIgnoreCase("partly"))
			clicked.addTag(tagname, "yes");
		else if (value.equalsIgnoreCase("yes"))
			clicked.removeTag(tagname);

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
		po.strokeColor(w.getStrokeColor(tagname)).fillColor(w.getFillColor(tagname));
		for (Coordinate c : w.getCoordinates())
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
		            	   newlyInsertet.addTag(tagname, "no");
		            	   break;
	            	   case 1:
		            	   newlyInsertet.addTag(tagname, "partly");
		            	   break;
	            	   case 2:
		            	   newlyInsertet.addTag(tagname, "yes");
		            	   break;
	            	   }
	            	   
	           }
	    });
	    return builder.create();
	}
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
				con.execute();
				((MainActivity) getActivity()).reOrder();
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
