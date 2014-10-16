package de.uni_hannover.inma.view;

import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.uni_hannover.inma.R;
import de.uni_hannover.inma.model.MainActivity;
import de.uni_hannover.inma.model.Tag;

public class Umgebung extends ListFragment {

	List<Tag> tags;
	
	public Umgebung(List<Tag> ways) {
		this.tags = ways;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.list_view, container,
				false);

		return rootView;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We need to use a different list item layout for devices older than Honeycomb
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
        setListAdapter(new ArrayAdapter<Tag>(getActivity(), layout, tags));
        }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);

        ((MainActivity) getActivity()).showMap(tags.get(position));
    }
}
