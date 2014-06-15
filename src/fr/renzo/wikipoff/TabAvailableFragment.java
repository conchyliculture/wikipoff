package fr.renzo.wikipoff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
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
	private ArrayList<Wiki> availablewikis=new ArrayList<Wiki>();
	private ListView availablewikislistview;
	private Context context;
	private int testprogr=0;
	private ArrayList<Integer> currentdownloads=new ArrayList<Integer>();
	private View wholeview;
	private DownloadFile downloadFile;

	private static int ERROR_URL_NOT_FOUND=0;

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

	private ArrayList<Wiki> loadAvailableDB() throws IOException {
		InputStream xml = copyXML(context.getString(R.string.available_xml_file));
		return WikiXMLParser.loadAvailableDBFromXML(context,xml);
	}

	private InputStream copyXML(String xml) throws IOException {
		AssetManager am = context.getAssets();
		try {
			InputStream in = am.open(xml);
			File outFile = new File(Environment.getExternalStorageDirectory(),context.getString(R.string.available_xml_file_external_path));
			if (!outFile.exists()) {
				FileOutputStream out = new FileOutputStream(outFile);

				byte[] buffer = new byte[1024];
				int read;
				while((read = in.read(buffer)) != -1){
					out.write(buffer, 0, read);
				}
				in.close();
				out.flush();
				out.close();
			}
			return new FileInputStream(outFile);
		} catch (IOException e) {
			e.printStackTrace();
			return am.open(xml);
		}
	}

	private void updateProgressBar(int position, int progress){
		View v = availablewikislistview.getChildAt(position);
		if (v!=null) {
			// TODO display ETA
			ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
			pb.setProgress(progress);
			if (progress != testprogr) {
				Log.d(TAG,"progress : "+progress+" position: "+position);
				testprogr=progress;
			}
		}
	}



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final int index = position;
		Wiki wiki = this.availablewikis.get(index);
		boolean missing=true;
		if (! currentdownloads.contains((Integer) position)) {
			try {
				missing = wiki.isMissing();
			} catch (WikiException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			if (missing) {
				this.download(index);

			} else {
				Toast.makeText(context, "The wiki is already installed ", Toast.LENGTH_LONG).show();
			}
		}

	}


	private void do_download(int position) {
		Wiki wiki = availablewikis.get(position);
		View v = availablewikislistview.getChildAt(position);
		if (v!=null) {
			ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
			pb.setVisibility(ProgressBar.VISIBLE);
		}
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
				.setMessage("Are you sure you want to download "+wiki.getLanglocal()+" ("+wiki.getSizeReadable(true)+")")
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
				.setMessage("Are you sure you want to download this huge file ("+wiki.getSizeReadable(true)+") without WIFI?")
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
			bot.setText(w.getFilenamesAsString()+" "+w.getLocalizedGendate());

			return convertView;
		}
	}

	class DownloadFile extends AsyncTask<Object, Integer, String> {

		private Integer position;

		protected String doInBackground(Object... params) {

			String result="";

			Wiki w = (Wiki)params[0];
			this.position = (Integer) params[1];
			File outputdir = Environment.getExternalStoragePublicDirectory(context.getString(R.string.DBDir));
			try {
				for(WikiDBFile wdbf : w.getDBFiles()) {
					URL url = new URL(wdbf.getUrl());
					download(url,new File(outputdir,wdbf.getFilename()));
				}
			} catch (SocketTimeoutException e) {
				Log.d(TAG,"Timeout...");
				result="failed";
				e.printStackTrace();
			} catch (MalformedURLException e) {
				Log.d(TAG,"MalformedURLException");
				result="failed";
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG,"IOException"+e.getMessage());
				//Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				result="failed";
				publishProgress(new Integer[]{0,ERROR_URL_NOT_FOUND});
				e.printStackTrace();
			}
			return result;
		}

		private String download(URL url,File file) throws IOException {
			String result="";
			URLConnection connection = url.openConnection();
			connection.connect();
			long fileLength = connection.getContentLength();

			URLConnection con = url.openConnection();
			con.setConnectTimeout(3000);
			con.setReadTimeout(3000);
			con.setDoOutput(true);
			InputStream input = new BufferedInputStream(con.getInputStream());


			OutputStream output = new FileOutputStream(file);
			byte data[] = new byte[8192];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				total += count;
				publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
				if(isCancelled()){
					result="stopped";
					break;
				}
			}
			output.flush();
			output.close();
			input.close();

			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("stopped")) {
				stopDownload(position, true);
			}else if (result.equals("failed")) {
				stopDownload(position, true);
			} else {
				stopDownload(position,false);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			if (progress.length>1) {
				int error=progress[1];
				displayDownloadError(error);
			}
			updateProgressBar(position,progress[0]);
		}

	}
	public void stopDownload(int position,boolean delete) {
		File outputdir = Environment.getExternalStoragePublicDirectory(context.getString(R.string.DBDir));
		Wiki wiki = availablewikis.get(position);
		currentdownloads.remove((Integer)position);
		View v = availablewikislistview.getChildAt(position);
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
		pb.setVisibility(View.GONE);
		if (delete) {
			for(String filename : wiki.getDBFilesnamesAsList()) {
				File db=new File(outputdir,filename);
				if (db.exists()) {
					db.delete();
				}
			}
		}
	}
	public void stopAsync(int pos) {
		if (currentdownloads.contains(Integer.valueOf(pos))) {
			this.downloadFile.cancel(true);
			stopDownload(pos, true);
		}
	}
	public void displayDownloadError(int error){
		String message="";
		switch (error) {
		case 0: //TODO
			message="Url not found. Check available_wikis.xml";
			break;

		default:
			break;
		}
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
