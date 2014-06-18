package fr.renzo.wikipoff;

import java.util.Collection;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

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
		Log.d(TAG,"isin : "+names);
		return (this.currentdownloads.containsKey(names));
	}
	public Collection<String> getCurrentDownloads() {
		
		return (this.currentdownloads.values());
	}
}

