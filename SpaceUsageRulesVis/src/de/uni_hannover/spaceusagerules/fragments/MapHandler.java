package de.uni_hannover.spaceusagerules.fragments;

import android.content.res.AssetManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import de.uni_hannover.spaceusagerules.LocationUpdateListener;
import de.uni_hannover.spaceusagerules.R;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.KML;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Way;

/**
 * Created by gumulka on 10/11/14.
 */
public class MapHandler extends SupportMapFragment {
    public final static String ARG_POSITION = "position";
    double latmin =10000, latmax = -10000, lonmin = 10000, lonmax = -10000;
    GoogleMap mMap;

    public void onStart() {
        super.onStart();


        mMap = getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateMapView(args.getInt(ARG_POSITION));
        }
    }
    private static LatLng toLatLon(Coordinate c) {
        return new LatLng(c.latitude,c.longitude);
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
        for (Coordinate c : w.getCoordinates()) {
            if(c.latitude<latmin)
                latmin = c.latitude;
            if(c.longitude<lonmin)
                lonmin = c.longitude;
            if(c.latitude>latmax)
                latmax = c.latitude;
            if(c.longitude>lonmax)
                lonmax = c.longitude;
        }
    }

    public void updateMapView(int position) {
        mMap.clear();

        MarkerOptions mo = new MarkerOptions();
        mo.title(getResources().getString(R.string.your_position));
        LatLng my = new LatLng(LocationUpdateListener.getLocation().getLatitude(),LocationUpdateListener.getLocation().getLongitude());
        mo.position(my);
        mMap.addMarker(mo);

        latmin =  10000;
        latmax = -10000;
        lonmin =  10000;
        lonmax = -10000;

        if(position==0) {
            for(Way w : OSM.getWays())
                updateMapPart(w);
        }
        else
            updateMapPart(OSM.getWays().get(position));

        LatLngBounds blub = new LatLngBounds(new LatLng(latmin, lonmin), new LatLng(latmax,lonmax));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(blub.getCenter(), 18));
    }


    public void updateCupView(int position) {
        mMap.clear();

        latmin =  10000;
        latmax = -10000;
        lonmin =  10000;
        lonmax = -10000;

        String filename = String.format("%04d.truth.kml",position+1);
        AssetManager assetManager = getActivity().getAssets();
        StringBuilder total = new StringBuilder();
        try {
            InputStream ins = assetManager.open(filename);

            BufferedReader r = new BufferedReader(new InputStreamReader(ins));
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line + "\n");
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
        List<Coordinate> coords = KML.loadKML(total.toString());
        Way w = new Way("truth");
        w.addAllCoordinates(coords);
        w.addTag("InformatiCup", "truth");
        updateMapPart(w);

        try {
            InputStream ins = assetManager.open("Data.txt");

            BufferedReader r = new BufferedReader(new InputStreamReader(ins));
            String line;
            MarkerOptions mo = null;
            while ((line = r.readLine()) != null) {
                String[] data = line.split(",");
                if(data.length==1)
                    continue;
                if(Integer.parseInt(data[0]) == (position+1)) {
                    if(mo==null) {
                        mo = new MarkerOptions();
                        LatLng my = new LatLng(Double.parseDouble(data[1]),Double.parseDouble(data[2]));
                        mo.position(my);
                        mo.title("");
                    }
                    mo.title(mo.getTitle() + data[3]);
                }
            }
            mMap.addMarker(mo);
        }catch(IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream ins = assetManager.open("MyData.txt");

            BufferedReader r = new BufferedReader(new InputStreamReader(ins));
            String line;
            MarkerOptions mo = null;
            while ((line = r.readLine()) != null) {
                String[] data = line.split(" ");
                if(data.length==1)
                    continue;
                if(Integer.parseInt(data[0]) == (position+1)) {
                    if(mo==null) {
                        mo = new MarkerOptions();
                        LatLng my = new LatLng(Double.parseDouble(data[1]),Double.parseDouble(data[2]));
                        mo.position(my);
                        mo.title("exact Location");
                        break;
                    }
                }
            }
            mMap.addMarker(mo);
        }catch(IOException e) {
            e.printStackTrace();
        }
        LatLngBounds blub = new LatLngBounds(new LatLng(latmin, lonmin), new LatLng(latmax,lonmax));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(blub.getCenter(), 18));
    }

    public void addOSMData() {
        for(Way w : OSM.getWays()) {
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

            MarkerOptions mo = new MarkerOptions();
            mo.title("");
            for(Map.Entry<String, String> e : w.getTags().entrySet())
                mo.title(mo.getTitle() + "\n" + e.getKey() + " -> " + e.getValue());
            mo.position(toLatLon(w.getCoordinates().get(0)));
            mMap.addMarker(mo);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
//        outState.putInt(ARG_POSITION, mCurrentPosition);
    }

}
