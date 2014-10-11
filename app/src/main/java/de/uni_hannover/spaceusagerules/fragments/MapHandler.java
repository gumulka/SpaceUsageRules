package de.uni_hannover.spaceusagerules.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import de.uni_hannover.spaceusagerules.R;
import de.uni_hannover.spaceusagerules.core.LocationUpdateListener;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.core.Way;

/**
 * Created by gumulka on 10/11/14.
 */
public class MapHandler extends SupportMapFragment {
    public final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
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
        } else if (mCurrentPosition != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateMapView(mCurrentPosition);
        }
    }

    private static LatLng centroid(List<LatLng> points) {
        double[] centroid = { 0.0, 0.0 };

        for (LatLng l : points) {
            centroid[0] += l.latitude;
            centroid[1] += l.longitude;
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return new LatLng(centroid[0], centroid[1]);
    }


    private void updateMapPart(Way w) {
        if(!w.isValid())
            return;
        if(w.isArea()) {
            PolygonOptions po = new PolygonOptions();
            po.strokeWidth(2);
            po.strokeColor(w.getStrokeColor()).fillColor(w.getFillColor());
            for (LatLng c : w.getCoordinates())
                po.add(c);
            mMap.addPolygon(po);
        }
        else {
            PolylineOptions po = new PolylineOptions();
            po.color(w.getStrokeColor());
            po.width(5);
            for (LatLng c : w.getCoordinates())
                po.add(c);
            mMap.addPolyline(po);
        }
        for (LatLng c : w.getCoordinates()) {
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


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }

}
