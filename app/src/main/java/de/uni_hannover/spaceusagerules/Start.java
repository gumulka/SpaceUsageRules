package de.uni_hannover.spaceusagerules;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import de.uni_hannover.spaceusagerules.core.OSM;
import de.uni_hannover.spaceusagerules.fragments.Cup;
import de.uni_hannover.spaceusagerules.fragments.MapHandler;
import de.uni_hannover.spaceusagerules.fragments.Results;


public class Start extends FragmentActivity implements Results.OnHeadlineSelectedListener , Cup.OnCupSelectedListener{
    private TextView textView;
    private static Start mApp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = this;
        setContentView(R.layout.activity_start);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    private void showCup() {

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.results_fragment);
        if(f instanceof Cup) {
            Toast.makeText(getApplicationContext(), "Wird bereits angezeigt.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create fragment and give it an argument for the selected article
        Cup newFragment = new Cup();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.results_fragment, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void showPos() {

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.results_fragment);
        if(f instanceof Results) {
            Toast.makeText(getApplicationContext(), "Wird bereits angezeigt.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create fragment and give it an argument for the selected article
        Results newFragment = new Results();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.results_fragment, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_cup:
                showCup();
                return true;
            case R.id.menu_position:
                showPos();
                return true;
            case R.id.menu_help:
//                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    public void onArticleSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        MapHandler mapFrag = (MapHandler)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        if (mapFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            mapFrag.updateMapView(position);

        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            MapHandler newFragment = new MapHandler();
            Bundle args = new Bundle();
            args.putInt(MapHandler.ARG_POSITION, position);
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.results_fragment, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

            TextView status = (TextView) findViewById(R.id.status);
            status.setText(OSM.getWays().get(position).toString());
        }
    }


    public void onCupSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        MapHandler mapFrag = (MapHandler)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        if (mapFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            mapFrag.updateCupView(position);

        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            MapHandler newFragment = new MapHandler();
            Bundle args = new Bundle();
            args.putInt(MapHandler.ARG_POSITION, position);
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.results_fragment, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

            TextView status = (TextView) findViewById(R.id.status);
            status.setText(OSM.getWays().get(position).toString());
        }
    }

    public static Context context() {
        return mApp;
    }
}
