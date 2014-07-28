package fr.renzo.wikipoff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

	private WikipOff app;
	private String storage;

	public static final int REQUEST_DELETE_CODE = 1001;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (WikipOff) getApplication();
		SharedPreferences config= PreferenceManager.getDefaultSharedPreferences(this);
		this.storage = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage());
		
		setTitle(getString(R.string.title_manage_wikis));
		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setContentView(R.layout.activity_manage_databases);
		installedFragment = new TabInstalledFragment();
		availableFragment = new TabAvailableFragment();
		Tab installedTab = bar.newTab().setText(getString(R.string.title_installed_wikis));
		Tab availableTab = bar.newTab().setText(getString(R.string.title_available_wikis));
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
		.setTitle(getString(R.string.message_warning))
		.setMessage(
				getString(R.string.message_overwrite_file,getString(R.string.available_xml_file)))
		.setNegativeButton(getString(R.string.no), null)
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
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
				
				File outFile = new File(storage,getString(R.string.available_xml_file_external_path));

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
		this.app.currentdownloads.put(Integer.valueOf(position), names);
	}
	public void deleteFromCurrentDownloads(int position) {
		this.app.currentdownloads.remove(Integer.valueOf(position));
	}
	public boolean isInCurrentDownloads(int position) {
		return (this.app.currentdownloads.containsKey(Integer.valueOf(position)));
	}
	public boolean isInCurrentDownloads(String names) {
		return (this.app.currentdownloads.containsKey(names));
	}
	public Collection<String> getCurrentDownloads() {
		return (this.app.currentdownloads.values());
	}
//	public String showCurrentDownloads() {
//		String res="Current dls : ";
//		Collection<String> lol = getCurrentDownloads();
//		for (Iterator<String> iterator = lol.iterator(); iterator.hasNext();) {
//			String string = iterator.next();
//			res+=string+", ";
//		}
//		return res;
//	}
	
	public String getStorage() {
		return this.storage;
	}
}

