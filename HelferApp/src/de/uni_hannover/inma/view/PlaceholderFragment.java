package de.uni_hannover.inma.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.uni_hannover.inma.IDs;
import de.uni_hannover.inma.MainActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
	
	
	private int layoutID = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null)
        	layoutID = args.getInt(IDs.LAYOUT_ID);

		if (savedInstanceState != null) {
			if(layoutID!=0 && layoutID != -1)
				layoutID = savedInstanceState.getInt(IDs.LAYOUT_ID);
        }

		View rootView = inflater.inflate(layoutID, container,
				false);
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		((MainActivity) getActivity()).setDirty(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		((MainActivity) getActivity()).setDirty(true);
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(IDs.LAYOUT_ID, layoutID);
    }
}