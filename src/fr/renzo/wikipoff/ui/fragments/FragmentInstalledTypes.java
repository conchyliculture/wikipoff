package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentInstalledTypes extends SherlockListFragment {

	private static final String TAG = "FragmentInstalledTypes";
	private View wholeview;
	private WikiManagerActivity manageractivity;
	private ArrayList<String> wikitypes;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		manageractivity = (WikiManagerActivity) getSherlockActivity();
		this.wikitypes = manageractivity.getWikiTypes();
		wholeview=inflater.inflate(R.layout.fragment_wiki_manager_installed,container, false);

		setListAdapter(new ArrayAdapter<String>(manageractivity,
				android.R.layout.simple_list_item_1,
				this.wikitypes)
				);
		
		return wholeview;
	}

//	// The following code shows how to properly open new fragment. It assumes
//	// that parent fragment calls its activity via interface. This approach
//	// is described in Android development guidelines.
//	@Override
//	public void onItemSelected(String item)
//	{
//		ItemFragment fragment = new ItemFragment();
//		Bundle args = new Bundle();
//		args.putString("item", item);
//		fragment.setArguments(args);
//		addFragment(fragment);
//	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG,"Clicked on "+position+" type:"+this.wikitypes.get(position));
		FragmentInstalledWikis fragment = new FragmentInstalledWikis();
		Bundle args = new Bundle();
		args.putString("type", this.wikitypes.get(position));
		fragment.setArguments(args);
		manageractivity.addFragment(fragment);
	}

}
