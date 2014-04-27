package fr.renzo.wikipoff;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
	private int testprogr=0;
	private ArrayList<Integer> currentdownloads=new ArrayList<Integer>();
	private View wholeview;
	private DownloadFile downloadFile;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context=getActivity();
		wholeview=inflater.inflate(R.layout.fragment_tab_available,null);
		if (savedInstanceState==null) {
		try {
			this.availablewikis=loadAvailableDB();
		} catch (IOException e) {
			Toast.makeText(context, "Problem opening available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
		availablewikislistview= (ListView) wholeview.findViewById(R.id.availablewikislistview);
		availablewikislistview.setOnItemClickListener(this);
		availablewikislistview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (currentdownloads.contains(Integer.valueOf(position))) {
				
				Intent outputintent = new Intent(context, StopDownloadActivity.class);
				outputintent.putExtra("position", position);
				startActivity(outputintent);
				}
				return true;
			}
		});
		AvailableWikisListViewAdapter adapter = new AvailableWikisListViewAdapter(getActivity(),  this.availablewikis); 
		availablewikislistview.setAdapter(adapter);
		}
		return wholeview ;
	}

	private void updateProgressBar(int position, int progress){
		View v = availablewikislistview.getChildAt(position);
		if (v!=null) {
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
		pb.setVisibility(ProgressBar.VISIBLE);
		pb.setProgress(progress);
		if (progress != testprogr) {
		Log.d(TAG,"progress : "+progress+" position: "+position);
			testprogr=progress;
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
		File already_there=null;
		if (! currentdownloads.contains((Integer) position)) {
		try {
			already_there = wiki.isAlreadyInstalled();
		} catch (WikiException e) {
			//Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
			if (already_there == null) {
				this.download(index);

			} else {
				Toast.makeText(context, "The wiki is already installed "+ already_there, Toast.LENGTH_LONG).show();
			}
		}

	}


	private void do_download(int position) {
		Wiki wiki = availablewikis.get(position);
		this.downloadFile = new DownloadFile();
		downloadFile.execute(wiki,Integer.valueOf(position));
	}
	

	private void download(final int position) {
		if (currentdownloads.contains(position)) {
			Toast.makeText(context, "Download already running", Toast.LENGTH_LONG).show();
		} else {
			this.currentdownloads.add((Integer)position);
		Wiki wiki = this.availablewikis.get(position);
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected()) {
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
	
	class DownloadFile extends AsyncTask<Object, Integer, String> {
	    
	    private Integer position;

		protected String doInBackground(Object... params) {
			String result="";
	    	try {
	    		Wiki w = (Wiki)params[0];
	    		this.position = (Integer) params[1];
	            URL url = new URL(w.getUrl());

	            URLConnection connection = url.openConnection();
	            connection.connect();
	            long fileLength = connection.getContentLength();

	            URLConnection con = url.openConnection();
	            con.setConnectTimeout(3000);
	            con.setReadTimeout(3000);
	            con.setDoOutput(true);
	            InputStream input = new BufferedInputStream(con.getInputStream());
	            File outputdir = Environment.getExternalStoragePublicDirectory(context.getString(R.string.DBDir));

	            OutputStream output = new FileOutputStream(new File(outputdir,w.getFilename()));
	            Log.d(TAG, "Lenght of file: " + fileLength);
	            byte data[] = new byte[8192];
	            long total = 0;
	            int count;
	            while ((count = input.read(data)) != -1) {
	            	total += count;
	            	publishProgress((int) (total * 100 / fileLength));
	            	output.write(data, 0, count);
	            	if(isCancelled()){
	            		Log.d(TAG,"Detected stop");
	            		result="stopped";
	            		break;
	            	}
	            }
	            Log.d(TAG,"Finished");
	            output.flush();
	            output.close();
	            input.close();
	    	} catch (SocketTimeoutException e) {
	    		Log.d(TAG,"Timeout...");
	    		result="failed";
	    		stopDownload(position,true);
				e.printStackTrace();
	    	} catch (MalformedURLException e) {
	    		Log.d(TAG,"MalformedURLException");
	    		result="failed";
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG,"IOException");
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				result="failed";
				e.printStackTrace();
			}
	        return result;
	    }
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        Log.d(TAG,"Starting new download");
	    }
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        Log.d(TAG,"postexec");
	        if (result.equals("stopped")) {
	        	stopDownload(position, true);
	        }else if (result.equals("failed")) {
	        	stopDownload(position, true);
			}
	    }

	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	       updateProgressBar(position,progress[0]);
	    }

	}
	public void stopDownload(int position,boolean delete) {
        File outputdir = Environment.getExternalStoragePublicDirectory(context.getString(R.string.DBDir));
        Wiki wiki = availablewikis.get(position);
        currentdownloads.remove((Integer)position);
        View v = availablewikislistview.getChildAt(position);
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
		TextView t = (TextView) v.findViewById(R.id.availablewikiheader);
		pb.clearAnimation();
		pb.setVisibility(View.INVISIBLE);
		pb.clearAnimation();
		Log.d(TAG,"Stop Dl, bar invisible lol"+pb.getVisibility()+" "+t.getText());
        if (delete) {
        File db=new File(outputdir,wiki.getFilename());
		if (db.exists()) {
			db.delete();
		}
        }
	}
	public void stopAsync(int pos) {
		if (currentdownloads.contains(Integer.valueOf(pos))) {
		this.downloadFile.cancel(true);
		stopDownload(pos, true);
		Log.d(TAG,"stoping");
		}
	}
}
