package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockListFragment;

import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentInstalledWikis extends SherlockListFragment {

	protected static final String TAG = "FragmentInstalledWikis";
	private View wholeview;
	private WikiManagerActivity manageractivity;
	private ArrayList<Wiki> wikis;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String type = getArguments().getString("type","putepedia");
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		this.wikis = manageractivity.getWikiByTypes(type);
		wholeview=inflater.inflate(R.layout.fragment_wiki_manager_installed,container, false);
		
		

		setListAdapter(new InstalledWikisListViewAdapter(manageractivity,this.wikis));
		
		return wholeview;
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG,"Clicked on "+position);
		
	}
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		registerForContextMenu(getListView());
//	}
//	
//	@Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        Log.d(TAG,"pute");
//        getActivity().getMenuInflater().inflate(R.menu.installed_wiki_context_menu,  menu);
//    }
//	
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
