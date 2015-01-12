package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
	private String type;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		type = getArguments().getString("type");
		manageractivity = (WikiManagerActivity) getSherlockActivity();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.wikis = manageractivity.getAvailableWikiByTypes(type);
		setListAdapter(new AvailableWikisListViewAdapter(manageractivity,this.wikis));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Wiki wiki = wikis.get(position);

		Intent myIntent;
		if (wiki.isMissing()) {
			myIntent = new Intent(getSherlockActivity(), WikiAvailableActivity.class);
		} else {
			myIntent = new Intent(getSherlockActivity(), WikiInstalledActivity.class);
		}
		
		myIntent.putExtra("wiki",  wiki);
		myIntent.putExtra("storage", manageractivity.storage);
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
				convertView = this.inflater.inflate(R.layout.available_wiki_item, parent, false);
			}
			

			TextView header = (TextView ) convertView.findViewById(R.id.availablewikiheader);
			header.setText(w.getLanglocal()+"("+w.getLangcode()+")"+" "+w.getType());
			TextView bot = (TextView ) convertView.findViewById(R.id.availablewikifooter);
			String bottext= w.getFilenamesAsString()+"("+w.getSizeReadable(true)+") "+w.getLocalizedGendate();
			TextView infos = (TextView) convertView.findViewById(R.id.availablewikiinfo);
			int isLocalWikiNewer = manageractivity.alreadyDownloaded(w);
			switch (isLocalWikiNewer) {
			case Wiki.WIKIEQUAL:
				infos.setText(" *"+manageractivity.getString(R.string.message_available_wiki_equal)+"*");
				break;
			case Wiki.WIKIOLDER: // Wiki on SD is older
				infos.setText(" *"+manageractivity.getString(R.string.message_available_wiki_older)+"*");
				break;
			case Wiki.WIKINEWER: // Wiki on SD is newer
				infos.setText(" *"+manageractivity.getString(R.string.message_available_wiki_newer)+"*");
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
