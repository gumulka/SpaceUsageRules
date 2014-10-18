package de.uni_hannover.inma;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import de.uni_hannover.inma.view.AddTagListFragment;
import de.uni_hannover.inma.view.PlaceholderFragment;
import de.uni_hannover.inma.view.AddTagListFragment.OnTagSelectedListener;
import de.uni_hannover.spaceusagerules.core.Coordinate;
import de.uni_hannover.spaceusagerules.core.Way;

public class AddTagActivity extends ActionBarActivity implements OnTagSelectedListener{
    
	Coordinate location = null;
	List<Way> ways = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_tag);

	    // Get the message from the intent
		
		if (savedInstanceState == null) {
		    Intent intent = getIntent();
		    ways = (List<Way>) intent.getSerializableExtra(IDs.WAYS);
		    location = (Coordinate) intent.getSerializableExtra(IDs.LOCATION);
		    
			Fragment frag = new AddTagListFragment();
            Bundle args = new Bundle();
            args.putSerializable(IDs.POSSIBILITIES, intent.getSerializableExtra(IDs.POSSIBILITIES));
            frag.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, frag).commit();
		}
		else {
			ways = (List<Way>) savedInstanceState.getSerializable(IDs.WAYS);
			location = (Coordinate) savedInstanceState.getSerializable(IDs.LOCATION);
		}
	}

	@Override
	public void onTagSelected(String tagname) {
		Fragment newFragment = new ShowMapActivity();
		Bundle args = new Bundle();
	    args.putSerializable(IDs.LOCATION, location);
	    args.putSerializable(IDs.WAYS, (Serializable) ways);
	    args.putSerializable(IDs.TAGNAME, tagname);
		newFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, newFragment).addToBackStack(null).commit();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putSerializable(IDs.LOCATION, (Serializable) location);
		outState.putSerializable(IDs.WAYS, (Serializable) ways);
	}
	
	
	
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_tag, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
