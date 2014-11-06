package de.uni_hannover.inma.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.uni_hannover.inma.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class HelpFragment extends Fragment {
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.first_use, container,
				false);
		return rootView;
	}
	
}