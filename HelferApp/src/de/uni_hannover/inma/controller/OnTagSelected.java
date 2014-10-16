package de.uni_hannover.inma.controller;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import de.uni_hannover.inma.view.AddTagFragment;

public class OnTagSelected implements OnItemClickListener{

	AddTagFragment fragment;
	
	public OnTagSelected(AddTagFragment f) {
		this.fragment = f;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		fragment.selectedItem(id);
		
	}

}
