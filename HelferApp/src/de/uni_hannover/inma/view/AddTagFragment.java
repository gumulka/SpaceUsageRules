package de.uni_hannover.inma.view;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import de.uni_hannover.inma.R;
import de.uni_hannover.inma.controller.OnTagSelected;
import de.uni_hannover.inma.model.MainActivity;

public class AddTagFragment extends Fragment {

	private List<String> possibilities;
	
	public AddTagFragment(List<String> possibilities) {
		this.possibilities = possibilities;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.add_tag, container,
				false);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, possibilities);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                rootView.findViewById(R.id.autoCompleteTextView1); // */
        ListView listView = (ListView)
        		rootView.findViewById(R.id.listView1);
        if(textView!=null)
        	textView.setAdapter(adapter);
        if(listView!=null)
        	listView.setAdapter(adapter);
        if(listView!=null)
        	listView.setOnItemClickListener(new OnTagSelected(this));

        return rootView;
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
	}
	
	public void selectedItem(long id) {
		((MainActivity) getActivity()).addTagToMap(possibilities.get((int) id));
	}
    
}
