package de.uni_hannover.spaceusagerules;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import de.uni_hannover.spaceusagerules.fragments.Cup;
import de.uni_hannover.spaceusagerules.fragments.MapHandler;
import de.uni_hannover.spaceusagerules.fragments.OnListItemSelected;
import de.uni_hannover.spaceusagerules.fragments.Results;


public class Start extends FragmentActivity implements OnListItemSelected {
    private static Start mApp = null;

    private CupUpdateListener cul = null;

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = null;
        switch (item.getItemId()) {
            case R.id.menu_cup:
                newFragment = new Cup();
                break;
            case R.id.menu_position:
                newFragment = new Results();
                break;
//            case R.id.menu_help:
//                break;
            case R.id.menu_settings:
                newFragment = new SettingsFragment();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        transaction.replace(R.id.results_fragment, newFragment).addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        return true;
    }


    public void onItemSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        MapHandler mapFrag = (MapHandler)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        Fragment listFragment = getSupportFragmentManager().findFragmentById(R.id.results_fragment);

        if (mapFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            if(listFragment instanceof Results)
                mapFrag.updateMapView(position);
            else if(listFragment instanceof Cup) {
                mapFrag.updateCupView(position);
                if(cul != null)
                    cul.makeObsolete();
                cul = new CupUpdateListener(position);
            }

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
        }
    }

    public static Context context() {
        return mApp;
    }
}
