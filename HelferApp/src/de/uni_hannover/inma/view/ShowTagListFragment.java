package de.uni_hannover.inma.view;

import java.io.Serializable;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.uni_hannover.inma.IDs;
import de.uni_hannover.spaceusagerules.core.Tag;

public class ShowTagListFragment extends ListFragment {

	OnTagSelectedListener mCallback;
	private List<Tag> taglist;
	private int lastClicked = -1;


	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnTagSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onTagSelected(Tag t);
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
			lastClicked = args.getInt(IDs.LAST_CLICKED, -1);
			taglist = (List<Tag>) args.getSerializable(IDs.TAGS);
        }
		if(savedInstanceState != null) {
			lastClicked = savedInstanceState.getInt(IDs.LAST_CLICKED, -1);
			taglist = (List<Tag>) savedInstanceState.getSerializable(IDs.TAGS);
		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			mCallback = (OnTagSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTagSelectedListener");
		}
	}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onStart() {
        super.onStart();
        // We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB 
				? android.R.layout.simple_list_item_activated_1	: android.R.layout.simple_list_item_1;
		if(taglist != null)
			setListAdapter(new ArrayAdapter<Tag>(getActivity(), layout,
				taglist));
		
        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.

		if(lastClicked!=-1)
	        // Set the item as checked to be highlighted when in two-pane layout
	        getListView().setItemChecked(lastClicked, true);
    }
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		View rootView = inflater.inflate(R.layout.add_tag, container, false);
//		if(savedInstanceState != null) {
//			lastClicked = savedInstanceState.getInt(IDs.LAST_CLICKED, -1);
//			possibilities = (List<String>) savedInstanceState.getSerializable(IDs.POSSIBILITIES);
//		}
//		if(lastClicked!=-1)
//	        // Set the item as checked to be highlighted when in two-pane layout
//	        getListView().setItemChecked(lastClicked, true);
//		
//		return rootView;
//	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putSerializable(IDs.POSSIBILITIES, (Serializable) taglist);
		outState.putInt(IDs.LAST_CLICKED, lastClicked);
	}


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Notify the parent activity of selected item
        mCallback.onTagSelected(taglist.get(position));
        
        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }

}
