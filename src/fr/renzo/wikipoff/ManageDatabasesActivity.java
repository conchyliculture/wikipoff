package fr.renzo.wikipoff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

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
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ManageDatabasesActivity extends SherlockFragmentActivity {
	public static final String TAG = "ManageDatabasesActivity";
	private TabInstalledFragment installedFragment;
	private TabAvailableFragment availableFragment;

	private WikipOff app;
	private String storage;
	private ActionBar bar;

	public static final int REQUEST_DELETE_CODE = 1001;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (WikipOff) getApplication();
		SharedPreferences config= PreferenceManager.getDefaultSharedPreferences(this);
		this.storage = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage());

		setTitle(getString(R.string.title_manage_wikis));
		bar = getSupportActionBar();
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
		if( savedInstanceState != null ){
			int state = savedInstanceState.getInt("tabState");
			bar.setSelectedNavigationItem(state);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);  // Fucking ugly but fix my problem of fragments created twice
		outState.putInt("tabState", bar.getSelectedTab().getPosition());
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
		getSupportMenuInflater().inflate(R.menu.managedbmenu, menu);
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

		protected String doInBackground(String... s) {
			String result="";
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
				result="failed "+e.getMessage();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result!="") {
				Toast.makeText(getApplicationContext(), "Error: "+result, Toast.LENGTH_LONG).show();
			}
			availableFragment.refreshList();
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

	public ArrayList<Wiki> getInstalledWikis(){
		// TODO cache?
		HashMap<String, Wiki> multiwikis = new HashMap<String, Wiki>();
		ArrayList<Wiki> res = new ArrayList<Wiki>();
		Collection<String> currendl = getCurrentDownloads();
		for (File f : new File(getStorage(),getString(R.string.DBDir)).listFiles()) {
			if (! f.getName().endsWith(".sqlite")) {
				continue;
			}
			String name = f.getName();
			if (name.indexOf("-")>0) {
				String root_wiki=name.substring(0, name.indexOf("-"));
				if (multiwikis.containsKey(root_wiki)){
					Wiki w = multiwikis.get(root_wiki);
					w.addDBFile(f);
				} else {
					try {
						Wiki w = new Wiki(this, f);
						multiwikis.put(root_wiki,w);
					} catch (WikiException e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			} else {
				try {
					if (! currendl.contains(f.getName())) {
						Wiki w = new Wiki(this,f);
						res.add(w);
					}
				} catch (WikiException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
		for (Wiki w : multiwikis.values()) {
			if (! currendl.contains(w.getFilenamesAsString()))
				res.add(w);
		}

		Collections.sort(res, new Comparator<Wiki>() {
			public int compare(Wiki w1, Wiki w2) {
				if (w1.getLangcode().equals(w2.getLangcode())) {
					return w1.getGendateAsDate().compareTo(w2.getGendateAsDate());
				} else {
					return w1.getLangcode().compareToIgnoreCase(w2.getLangcode());
				}
			}
		}
				);

		return res;
	}

	public String getStorage() {
		return this.storage;
	}

	public int alreadyDownloaded(Wiki w_tocheck) {
		ArrayList<Wiki> wikis = getInstalledWikis();
		for (Iterator<Wiki> iterator = wikis.iterator(); iterator.hasNext();) {
			Wiki w = (Wiki) iterator.next();
			if (w.getType().equals(w_tocheck.getType()) && w.getLangcode().equals(w_tocheck.getLangcode()) ){
				int res=w_tocheck.getGendateAsDate().compareTo(w.getGendateAsDate());
				return res;
			}
		}
		return -1; // We don't have that Wiki
	}
}

