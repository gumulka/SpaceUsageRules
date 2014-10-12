// The container Activity must implement this interface so the frag can deliver messages

package de.uni_hannover.spaceusagerules.fragments;

public interface OnListItemSelected {
    /** Called by HeadlinesFragment when a list item is selected */
    public void onItemSelected(int position);
}