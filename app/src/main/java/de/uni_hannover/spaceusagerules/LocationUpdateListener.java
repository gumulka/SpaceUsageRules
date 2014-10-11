package de.uni_hannover.spaceusagerules;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import de.uni_hannover.spaceusagerules.R;
import de.uni_hannover.spaceusagerules.Start;
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
            TextView statusText = (TextView) a.findViewById(R.id.status);
            statusText.setText(R.string.query_osm);
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
            OSM.createObjectList(urls[0]);
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void v) {
            TextView statusText = (TextView) a.findViewById(R.id.status);
            statusText.setText(R.string.data_ready);
            Results res = (Results) a.getSupportFragmentManager().findFragmentById(R.id.results_fragment);
            res.onOsmUpdate();
        } // */
    }
}
