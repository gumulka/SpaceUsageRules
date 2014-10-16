/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni_hannover.spaceusagerules.fragments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.uni_hannover.spaceusagerules.R;

public class Cup extends ListFragment {
    OnListItemSelected mCallback;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.list_view, container, false);
    }
    private Map<Integer,Integer> listToID = new TreeMap<Integer,Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> numbers = new LinkedList<String>();
        AssetManager assetManager = getActivity().getAssets();
        try {
            InputStream ins = assetManager.open("Data.txt");

            BufferedReader r = new BufferedReader(new InputStreamReader(ins));
            String line;
            while ((line = r.readLine()) != null) {
                String[] data = line.split(",");
                if(data.length==1)
                    continue;
                int id = Integer.parseInt(data[0]);
                if(listToID.values().contains(id))
                	continue;
                listToID.put(numbers.size(), id);
                numbers.add(getString(R.string.show_number) + " " + id);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
        // We need to use a different list item layout for devices older than Honeycomb
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;

        setListAdapter(new ArrayAdapter<String>(getActivity(), layout, numbers));
    }



    @Override
    public void onStart() {
        super.onStart();

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.map_fragment) != null) {
                getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnListItemSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCupSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);

        // Notify the parent activity of selected item
        mCallback.onItemSelected(listToID.get(position));
    }
}