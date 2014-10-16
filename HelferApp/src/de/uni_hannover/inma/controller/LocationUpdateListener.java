package de.uni_hannover.inma.controller;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;
import de.uni_hannover.inma.R;
import de.uni_hannover.inma.model.MainActivity;

/**
 * Created by gumulka on 10/10/14.
 */
public class LocationUpdateListener implements LocationListener {

        private static Location l;
        private MainActivity a;
        private boolean location_available = true;

        public LocationUpdateListener(MainActivity activity){
            this.a = activity;
        }

        public void onLocationChanged(Location location) {
            if(l!=null && l.distanceTo(location)<50)
                return;
            l = location;
        	a.updateLocation(l);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {
        	if(!location_available)
    			Toast.makeText(a, a.getString(R.string.found_location), Toast.LENGTH_SHORT).show();
        	location_available = true;
        }

        public void onProviderDisabled(String provider) {
        	location_available = false;
			Toast.makeText(a, a.getString(R.string.no_location), Toast.LENGTH_LONG).show();
			}

    public static Location getLocation() {
        return l;
    }

}
