package fr.renzo.wikipoff;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"created");
		setTitle("Manage your 'Wikis'");

		setContentView(R.layout.activity_manage_databases);

		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		if (savedInstanceState==null) {
			Tab installedTab = bar.newTab().setText("Downloaded Wikis");
			Tab availableTab = bar.newTab().setText("Available Wikis");

			installedFragment = new TabInstalledFragment();
			availableFragment = new TabAvailableFragment();

			installedTab.setTabListener(new MyTabsListener(installedFragment));
			availableTab.setTabListener(new MyTabsListener(availableFragment));

			bar.addTab(installedTab);
			bar.addTab(availableTab);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String command = extras.getString("command");
			if (command == null) {
				Log.d(TAG,"Need a command");
			} else if (command.equals("startdownload")) {
				final Wiki wiki =(Wiki) extras.getSerializable("wiki");
				do_download(wiki);
			} else if (command.equals("stopdownloads")) {
				//TabAvailableFragment fragment = (TabAvailableFragment) getSupportFragmentManager().findFragmentById(R.id.availablewikisfragment);
				Log.d(TAG,"allez");
				availableFragment.disableAllProgressBar();
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

	public void do_download(Wiki wiki){
		String url = wiki.getUrl();
		String filename = wiki.getFilename();
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription("Downloading from "+url);
		request.setTitle(filename);
		request.setDestinationInExternalPublicDir(this.getString(R.string.DBDir), filename);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		long dlid = ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
		Log.d(TAG,"pushed id "+dlid);
		//		Query q = new Query();
		//		q.setFilterById(dlid);
	}
}

