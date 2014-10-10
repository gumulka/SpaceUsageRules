package de.uni_hannover.spaceusagerules.core;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;

import de.uni_hannover.spaceusagerules.R;

/**
 * Created by gumulka on 10/10/14.
 */
public class LocationUpdateListener implements LocationListener {

        private Location l;
        private Activity a;

        public LocationUpdateListener(Activity activity){
            this.a = activity;
        }

        public void onLocationChanged(Location location) {
            if(l!=null && l.distanceTo(location)<0.0005)
                return;
            l = location;
            // Called when a new location is found by the network location provider.
            ConnectivityManager connMgr = (ConnectivityManager)
                    a.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask((Button) a.findViewById(R.id.mapButton)).execute(location);
            } // */
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}



    private class DownloadWebpageTask extends AsyncTask<Location, Void, Void> {
        private Button b;

        public DownloadWebpageTask(Button button) {
            this.b = button;
        }
        @Override
        protected Void doInBackground(Location... urls) {
            OSM.createObjectList(urls[0]);
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void a) {
            b.setClickable(true);

        } // */
    }
}
