package de.uni_hannover.spaceusagerules;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.model.LatLng;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.fragments.Results;

/**
 * Created by gumulka on 10/10/14.
 */
public class LocationUpdateListener implements LocationListener {

        private static Location l;
        private Start a;

        public LocationUpdateListener(Start activity){
            this.a = activity;
        }

        public void onLocationChanged(Location location) {
            if(l!=null && l.distanceTo(location)<50)
                return;
            l = location;
            // Called when a new location is found by the network location provider.
            ConnectivityManager connMgr = (ConnectivityManager)
                    a.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask(a).execute(location);
            } // */
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}

    public static Location getLocation() {
        return l;
    }

    private class DownloadWebpageTask extends AsyncTask<Location, Void, Void> {
        private Start a;

        public DownloadWebpageTask(Start activity) {
            this.a = activity;
        }
        @Override
        protected Void doInBackground(Location... urls) {
            Coordinate l = new Coordinate(urls[0].getLatitude(),urls[0].getLongitude());
            OSM.createObjectList(l);
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void v) {
            Fragment res = a.getSupportFragmentManager().findFragmentById(R.id.results_fragment);
            if(res instanceof Results)
                ((Results) res).onOsmUpdate();
        } // */
    }
}
