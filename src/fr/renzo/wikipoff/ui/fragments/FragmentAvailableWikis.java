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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.ui.activities.WikiAvailableActivity;
import fr.renzo.wikipoff.ui.activities.WikiInstalledActivity;
import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentAvailableWikis extends SherlockListFragment {

	protected static final String TAG = "FragmentAvailableWikis";
	private WikiManagerActivity manageractivity;
	private ArrayList<Wiki> wikis;
	private SharedPreferences config;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String type = getArguments().getString("type",null);
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		config= PreferenceManager.getDefaultSharedPreferences(manageractivity);
		this.wikis = manageractivity.getAvailableWikiByTypes(type);
		setListAdapter(new AvailableWikisListViewAdapter(manageractivity,this.wikis));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Wiki wiki = wikis.get(position);

		Intent myIntent = new Intent(getSherlockActivity(), WikiAvailableActivity.class);
		myIntent.putExtra("wiki",  wiki);
		startActivityForResult(myIntent,WikiManagerActivity.REQUEST_DELETE_CODE);
	}
	public class AvailableWikisListViewAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<Wiki> data;
		public AvailableWikisListViewAdapter(Context context, ArrayList<Wiki> data){
			this.inflater = LayoutInflater.from(context);
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
			if(convertView == null){ 
				convertView = this.inflater.inflate(R.layout.available_wiki, parent, false);
			}

			TextView header = (TextView ) convertView.findViewById(R.id.availablewikiheader);
			header.setText(w.getLanglocal()+"("+w.getLangcode()+")"+" "+w.getType());
			TextView bot = (TextView ) convertView.findViewById(R.id.availablewikifooter);
			String bottext= w.getFilenamesAsString()+"("+w.getSizeReadable(true)+") "+w.getLocalizedGendate();
			int isLocalWikiNewer = manageractivity.alreadyDownloaded(w);
			switch (isLocalWikiNewer) {
			case Wiki.WIKIEQUAL:
				bottext = bottext + " *installed*";
				break;
			case Wiki.WIKIOLDER:
				bottext = bottext + " *updated*";
				break;
			case Wiki.WIKINEWER:
				bottext = bottext + " *older than yours*";
				break;
			}
			bot.setText(bottext);

			ProgressBar pb = (ProgressBar) convertView.findViewById(R.id.downloadprogress);
			if (manageractivity.isInCurrentDownloads(Integer.valueOf(position))) {
				pb.setVisibility(View.VISIBLE);
			}

			return convertView;
		}
	}
	
}
