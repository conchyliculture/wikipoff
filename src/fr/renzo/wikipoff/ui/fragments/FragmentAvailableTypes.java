package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentAvailableTypes extends SherlockListFragment {
	@SuppressWarnings("unused")
	private static final String TAG = "FragmentAvailableTypes";
	private WikiManagerActivity manageractivity;
	private ArrayList<String> wikitypes;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		manageractivity = (WikiManagerActivity) getSherlockActivity();
	}

	@Override
	public void onResume() {
		super.onResume();
		setAdapter();
	}

	private void setAdapter(){
		this.wikitypes = manageractivity.getAvailableWikiTypes();
		setListAdapter(new ArrayAdapter<String>(manageractivity,
				android.R.layout.simple_list_item_1,
				this.wikitypes)
				);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FragmentAvailableWikis fragment = new FragmentAvailableWikis();
		Bundle args = new Bundle();
		args.putString("type", this.wikitypes.get(position));
		fragment.setArguments(args);
		manageractivity.addFragment(fragment);
	}
}
