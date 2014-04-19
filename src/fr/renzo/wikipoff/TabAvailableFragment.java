package fr.renzo.wikipoff;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TabAvailableFragment extends Fragment implements OnItemClickListener {
	private static final String TAG = "TabAvailableActivity";
	private static final String available_db_xml_file="available_wikis.xml";
	private ArrayList<Wiki> wikis=new ArrayList<Wiki>();
	private ListView availablewikislistview;
	private Context context;
	private View wholeview;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity().getApplicationContext();
        wholeview=inflater.inflate(R.layout.fragment_tab_available,null);
        try {
			loadDB();
		} catch (IOException e) {
			Toast.makeText(context, "Problem opening available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
        availablewikislistview= (ListView) wholeview.findViewById(R.id.availablewikislistview);
        ArrayAdapter<Wiki> adapter = new ArrayAdapter<Wiki>(getActivity(), android.R.layout.simple_list_item_1, this.wikis); 
        availablewikislistview.setAdapter(adapter);
        availablewikislistview.setOnItemClickListener(this);
        return wholeview ;
   }
    
    
    private void loadDB() throws IOException {
    	wikis.clear();
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
						this.wikis.add(curwiki);
						curwiki=null;
					} else if (endt.equalsIgnoreCase("type")) {
						curwiki.setType(curtext);
					} else if (endt.equalsIgnoreCase("lang")) {
						curwiki.setLang(curtext);
					} else if (endt.equalsIgnoreCase("url")) {
						curwiki.setUrl(curtext);
					} else if (endt.equalsIgnoreCase("gendate")) {
						curwiki.setGendate(curtext);
					} else if (endt.equalsIgnoreCase("version")) {
						curwiki.setVersion(curtext);
					}else if (endt.equalsIgnoreCase("filename")) {
						curwiki.setFilename(curtext);
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
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Wiki wiki = this.wikis.get(position);
		Log.d(TAG,"Clicked on "+wiki.toString());
		File already_there = wiki.isAlreadyInstalled();
		if (already_there == null) {
			Log.d(TAG, "we need to dl "+wiki.getUrl());
			this.download(wiki);
		} else {
			Toast.makeText(context, "The wiki is already installed "+ already_there, Toast.LENGTH_LONG).show();
		}
		
	}
	

	private void download(Wiki wiki) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final String url=wiki.getUrl();
		final String filename=wiki.getFilename();
		if (wifi.isConnected()) {
		    Log.d(TAG,"Using wifi!");
		    Intent i = new Intent(context.getApplicationContext(), ManageDatabasesActivity.class);
		    i.putExtra("filename",filename);
		    i.putExtra("url",url);
		    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    startActivity(i);
	   
		}else {
			new AlertDialog.Builder(context)
		    .setTitle("No Wifi detected")
		    .setMessage("Are you sure you want to download this huge file without WIFI?")
		    .setNegativeButton("No", null)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				    Intent i = new Intent(context.getApplicationContext(), ManageDatabasesActivity.class);
				    i.putExtra("filename",filename);
				    i.putExtra("url",url);
				    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				    startActivity(i);
				}
			})
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .show();
		}
	}
	

}
