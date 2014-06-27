package fr.renzo.wikipoff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import fr.renzo.wikipoff.TabAvailableFragment.DownloadFile;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ManageDatabasesActivity extends ActionBarActivity {
	public static final String TAG = "ManageDatabasesActivity";
	private TabInstalledFragment installedFragment;
	private TabAvailableFragment availableFragment;

	public HashMap<Integer,String> currentdownloads=new HashMap<Integer,String>();

	public static final int REQUEST_DELETE_CODE = 1001;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("Manage your 'Wikis'");
		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setContentView(R.layout.activity_manage_databases);
		installedFragment = new TabInstalledFragment();
		availableFragment = new TabAvailableFragment();
		Tab installedTab = bar.newTab().setText("Downloaded Wikis");
		Tab availableTab = bar.newTab().setText("Available Wikis");
		installedTab.setTabListener(new MyTabsListener(installedFragment));
		availableTab.setTabListener(new MyTabsListener(availableFragment));

		bar.addTab(installedTab);
		bar.addTab(availableTab);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String command = extras.getString("command");
			if (command == null) {
				Log.d(TAG,"Need a command");

			} else if (command.equals("stopdownload")) {
				int pos = extras.getInt("position");
				availableFragment.stopAsync(pos);
			}else {
				Log.d(TAG,"Unkown command : "+ command);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.managedbmenu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.action_update_available_wikis_xml:

			updateAvailableWikisXML();
			return true;
		case R.id.action_about:
			Intent i2 = new Intent(this, AboutActivity.class);
			startActivity(i2);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateAvailableWikisXML() {
		new AlertDialog.Builder(this)
		.setTitle("Warning")
		.setMessage("Are you sure you want to overwrite current "+getString(R.string.available_xml_file)+" file ?")
		.setNegativeButton("No", null)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				new DownloadXMLFile().execute(getString(R.string.available_xml_web_url));
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();

	}

	class DownloadXMLFile extends AsyncTask<String, Integer, String> {


		private String result;

		protected String doInBackground(String... s) {
			try {
				URL url = new URL(s[0]);
				
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				con.connect();
				InputStream input = new BufferedInputStream(con.getInputStream());

				File outFile = new File(Environment.getExternalStorageDirectory(),getString(R.string.available_xml_file_external_path));

				FileOutputStream out = new FileOutputStream(outFile,false);

				byte[] buffer = new byte[1024];
				int read;
				while((read = input.read(buffer)) != -1){
					out.write(buffer, 0, read);
				}
				input.close();
				out.flush();
				out.close();


			} catch (IOException e) {
				e.printStackTrace();
				this.result="failed "+e.getMessage();
			}
			return result;
		}

		protected void onPostExecute() {
	        if (this.result!="") {
	        	Toast.makeText(getApplicationContext(), "Error: "+result, Toast.LENGTH_LONG).show();
	        }
	    }

	}

	protected class MyTabsListener implements ActionBar.TabListener {

		private Fragment fragment;

		public MyTabsListener(Fragment fragment) {
			this.fragment = fragment;
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.add(R.id.fragment_container, fragment);
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}
	}

	public void addToCurrentDownloads(int position, String names) {
		this.currentdownloads.put(Integer.valueOf(position), names);
	}
	public void deleteFromCurrentDownloads(int position) {
		this.currentdownloads.remove(Integer.valueOf(position));
	}
	public boolean isInCurrentDownloads(int position) {
		return (this.currentdownloads.containsKey(Integer.valueOf(position)));
	}
	public boolean isInCurrentDownloads(String names) {
		return (this.currentdownloads.containsKey(names));
	}
	public Collection<String> getCurrentDownloads() {

		return (this.currentdownloads.values());
	}
}

