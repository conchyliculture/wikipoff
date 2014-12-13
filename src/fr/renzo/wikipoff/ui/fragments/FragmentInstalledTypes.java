package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentInstalledTypes extends SherlockListFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "FragmentInstalledTypes";
	private WikiManagerActivity manageractivity;
	private ArrayList<String> wikitypes;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		this.wikitypes = manageractivity.getWikiTypes();

		setListAdapter(new ArrayAdapter<String>(manageractivity,
				android.R.layout.simple_list_item_1,
				this.wikitypes)
				);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FragmentInstalledWikis fragment = new FragmentInstalledWikis();
		Bundle args = new Bundle();
		args.putString("type", this.wikitypes.get(position));
		fragment.setArguments(args);
		manageractivity.addFragment(fragment);
	}

}
