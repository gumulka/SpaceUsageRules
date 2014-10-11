package de.uni_hannover.spaceusagerules;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.fragments.MapHandler;

/**
 * Created by gumulka on 10/10/14.
 */
public class CupUpdateListener extends AsyncTask<Coordinate, Void, Void> {

    private boolean obsolete = false;

    public CupUpdateListener(int position) {
        // Called when a new location is found by the network location provider.
        ConnectivityManager connMgr = (ConnectivityManager)
                Start.context().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            AssetManager assetManager = Start.context().getAssets();
            try {
                InputStream ins = assetManager.open("Data.txt");

                BufferedReader r = new BufferedReader(new InputStreamReader(ins));
                String line;
                while ((line = r.readLine()) != null) {
                    String[] data = line.split(",");
                    if(data.length==1)
                        continue;
                    if(Integer.parseInt(data[0]) == (position+1)) {
                        Coordinate location = new Coordinate(Double.parseDouble(data[1]),Double.parseDouble(data[2]));
                        execute(location);
                        break;
                    }
                }
            }catch(IOException e) {
                e.printStackTrace();
            }
        } // */
    }

    protected Void doInBackground(Coordinate... urls) {
        OSM.createObjectList(urls[0]); //,(float) 0.0015);
        return null;
    }

    public void makeObsolete() {
        this.obsolete = true;
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Void v) {
        if(obsolete)
            return;
        MapHandler res = (MapHandler) ((Start) Start.context()).getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        res.addOSMData();
    } // */
}
