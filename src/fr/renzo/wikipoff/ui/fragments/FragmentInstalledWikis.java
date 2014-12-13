package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.ui.activities.WikiActivity;
import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentInstalledWikis extends SherlockListFragment {

	protected static final String TAG = "FragmentInstalledWikis";
	private WikiManagerActivity manageractivity;
	private ArrayList<Wiki> wikis;
	private SharedPreferences config;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String type = getArguments().getString("type",null);
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		config= PreferenceManager.getDefaultSharedPreferences(manageractivity);
		this.wikis = manageractivity.getWikiByTypes(type);
		setListAdapter(new InstalledWikisListViewAdapter(manageractivity,this.wikis));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Wiki wiki = wikis.get(position);

		Intent myIntent = new Intent(getSherlockActivity(), WikiActivity.class);
		myIntent.putExtra("wiki",  wiki);
		myIntent.putExtra("position",position);
		startActivityForResult(myIntent,WikiManagerActivity.REQUEST_DELETE_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode >=0 && resultCode < wikis.size()) {
			Wiki wiki = wikis.get(resultCode);
			String currseldb = config.getString(getString(R.string.config_key_selecteddbfiles), "");
			if (wiki.getFilenamesAsString().equals(currseldb)){
				config.edit().remove(getString(R.string.config_key_selecteddbfiles)).commit();
			}
			config.edit().putBoolean(getString(R.string.config_key_should_update_db), true).commit();
			manageractivity.refresh_installed_wikis=true;
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		((InstalledWikisListViewAdapter) getListView().getAdapter()).notifyDataSetInvalidated();
	}

	public class InstalledWikisListViewAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<Wiki> data;
		@SuppressWarnings("unused")
		private int selectedPosition = 0;
		public InstalledWikisListViewAdapter(Context context, ArrayList<Wiki> data){
			// Caches the LayoutInflater for quicker use
			this.inflater = LayoutInflater.from(context);
			// Sets the events data
			this.data= data;
		}
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			if(position < getCount() && position >= 0 ){
				return position;
			} else {
				return -1;
			}
		}



		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Wiki w = data.get(position);
			if(convertView == null){ // If the View is not cached
				// Inflates the Common View from XML file
				convertView = this.inflater.inflate(R.layout.installed_wiki_item, parent, false);
			}
			TextView header = (TextView ) convertView.findViewById(R.id.installedwikiheader);
			header.setText(w.getType()+" "+w.getLanglocal());
			TextView bot = (TextView ) convertView.findViewById(R.id.installedwikifooter);
			bot.setText(w.getFilenamesAsString()+" "+w.getLocalizedGendate());
			TextView rb = (TextView) convertView.findViewById(R.id.checked);
			if (w.isSelected()){
				rb.setText("\u2713");
			}			
			return convertView;

		}

	}

}
