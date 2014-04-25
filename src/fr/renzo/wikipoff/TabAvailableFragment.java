package fr.renzo.wikipoff;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TabAvailableFragment extends Fragment implements OnItemClickListener {
	private static final String TAG = "TabAvailableActivity";
	private static final String available_db_xml_file="available_wikis.xml"; // TODO : move in xml
	private ArrayList<Wiki> availablewikis=new ArrayList<Wiki>();
	private ListView availablewikislistview;
	private Context context;
	private View wholeview;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context=getActivity();
		wholeview=inflater.inflate(R.layout.fragment_tab_available,null);
		try {
			this.availablewikis=loadAvailableDB();
		} catch (IOException e) {
			Toast.makeText(context, "Problem opening available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
		availablewikislistview= (ListView) wholeview.findViewById(R.id.availablewikislistview);
		availablewikislistview.setOnItemClickListener(this);
		AvailableWikisListViewAdapter adapter = new AvailableWikisListViewAdapter(getActivity(),  this.availablewikis); 
		availablewikislistview.setAdapter(adapter);
		return wholeview ;
	}

	private void enableProgressBar(View v){
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
		pb.setVisibility(View.VISIBLE);
	}

	public  void disableAllProgressBar(){
		if (availablewikislistview!=null) {
			for (int i = 0; i < availablewikislistview.getChildCount(); i++) {
				View v = availablewikislistview.getChildAt(i);
				ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
				pb.setVisibility(View.INVISIBLE);
			}
		}
	}

	private ArrayList<Wiki> loadAvailableDB() throws IOException {
		ArrayList<Wiki> res = new ArrayList<Wiki>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(context.getAssets().open(available_db_xml_file)));
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(reader); 
			int eventType = parser.getEventType();

			String curtext = null;
			Wiki curwiki=null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					//   Log.d(TAG,"Start document");
					break;
				case XmlPullParser.START_TAG:
					//					Log.d(TAG,"Start tag "+parser.getName());
					String t=parser.getName();
					if (t.equalsIgnoreCase("wiki")){
						curwiki = new Wiki(context);
					}
					break;
				case XmlPullParser.TEXT:
					curtext = parser.getText();
					break;
				case XmlPullParser.END_TAG:
					//					Log.d(TAG,"End tag"+parser.getName());
					String endt=parser.getName();

					if (endt.equalsIgnoreCase("wiki")){
						res.add(curwiki);
						curwiki=null;
					} else if (endt.equalsIgnoreCase("type")) {
						curwiki.setType(curtext);
					} else if (endt.equalsIgnoreCase("lang-code")) {
						curwiki.setLangcode(curtext);
					} else if (endt.equalsIgnoreCase("lang-english")) {
						curwiki.setLangenglish(curtext);
					} else if (endt.equalsIgnoreCase("lang-local")) {
						curwiki.setLanglocal(curtext);
					} else if (endt.equalsIgnoreCase("url")) {
						curwiki.setUrl(curtext);
					} else if (endt.equalsIgnoreCase("gendate")) {
						curwiki.setGendate(curtext);
					} else if (endt.equalsIgnoreCase("version")) {
						curwiki.setVersion(curtext);
					}else if (endt.equalsIgnoreCase("filename")) {
						curwiki.setFilename(curtext);
					}else if (endt.equalsIgnoreCase("size")) {
						curwiki.setSize(Long.parseLong(curtext));
					}
					break;
				default:
					break;
				}

				eventType = parser.next();
			}
			//            Log.d(TAG,"End document");

		} catch (XmlPullParserException e) {
			Toast.makeText(context, "Problem parsing available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		Collections.sort(res, new Comparator<Wiki>() {
			public int compare(Wiki w1, Wiki w2) {
				if (w1.getLangcode().equals(w2.getLangcode())) {
					return w1.getGendate().compareTo(w2.getGendate());
				} else {
					return w1.getLangcode().compareToIgnoreCase(w2.getLangcode());
				}
			}
		});
		return res;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final int index = position;
		Wiki wiki = this.availablewikis.get(index);
		Log.d(TAG,"Clicked on "+wiki.toString());
		File already_there;
		try {
			already_there = wiki.isAlreadyInstalled();

			if (already_there == null) {
				Log.d(TAG, "we need to dl "+wiki.getUrl());
				this.download(index);

			} else {
				Toast.makeText(context, "The wiki is already installed "+ already_there, Toast.LENGTH_LONG).show();
			}
		} catch (WikiException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private Intent makeWikiIntent(Wiki wiki) {
		Intent i = new Intent(context, ManageDatabasesActivity.class);
		i.putExtra("command", "startdownload");
		i.putExtra("wiki", wiki);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return i;
	}

	private void do_download(int position) {
		Wiki wiki = availablewikis.get(position);
		View view = availablewikislistview.getChildAt(position);
		enableProgressBar(view);
		Intent i = makeWikiIntent(wiki);
		startActivity(i);
	}

	private void download(final int position) {
		Wiki wiki = this.availablewikis.get(position);
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected()) {
			Log.d(TAG,"Using wifi!");
			new AlertDialog.Builder(context)
			.setTitle("Warning")
			.setMessage("Are you sure you want to download "+wiki.getFilename()+" ("+wiki.getSizeReadable(true)+")")
			.setNegativeButton("No", null)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					do_download(position);
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
		} else {
			new AlertDialog.Builder(context)
			.setTitle("No Wifi detected")
			.setMessage("Are you sure you want to download this huge file ("+wiki.getSizeReadable(true)+"Kb) without WIFI?")
			.setNegativeButton("No", null)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					do_download(position);
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
		}
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
				convertView = this.inflater.inflate(R.layout.available_wiki, null);
			}
			TextView header = (TextView ) convertView.findViewById(R.id.availablewikiheader);
			header.setText(w.getType()+" "+w.getLanglocal());
			TextView bot = (TextView ) convertView.findViewById(R.id.availablewikifooter);
			bot.setText(w.getFilename()+" "+w.getLocalizedGendate());

			return convertView;
		}
	}
}
