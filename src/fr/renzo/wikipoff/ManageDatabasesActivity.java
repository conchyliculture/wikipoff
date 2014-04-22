package fr.renzo.wikipoff;

import java.util.HashMap;

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
import android.widget.Toast;

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

		Tab installedTab = bar.newTab().setText("Downloaded Wikis");
		Tab availableTab = bar.newTab().setText("Available Wikis");

		installedFragment = new TabInstalledFragment();
		availableFragment = new TabAvailableFragment();

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

	//    private void checkDownloadStatus(Context context){
	//		DownloadManager.Query query = new DownloadManager.Query();
	//		query.setFilterById(this.downloadid);
	//
	//		Cursor cursor = downloadManager.query(query);
	//		if(cursor.moveToFirst()){
	//			int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
	//			int status = cursor.getInt(columnIndex);
	//			int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
	//			int reason = cursor.getInt(columnReason);
	//			
	//			statusMessage(context, status, reason);
	//		}


	private void statusMessage(Context context, int status,int reason){
		switch(status){

		case DownloadManager.STATUS_FAILED:
			String failedReason = "unknown reason: "+reason;
			switch(reason){
			case 404:
				failedReason = "Download returned 404 error";
				break;
			case DownloadManager.ERROR_CANNOT_RESUME:
				failedReason = "ERROR_CANNOT_RESUME";
				break;
			case DownloadManager.ERROR_DEVICE_NOT_FOUND:
				failedReason = "ERROR_DEVICE_NOT_FOUND";
				break;
			case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
				failedReason = "ERROR_FILE_ALREADY_EXISTS";
				break;
			case DownloadManager.ERROR_FILE_ERROR:
				failedReason = "ERROR_FILE_ERROR";
				break;
			case DownloadManager.ERROR_HTTP_DATA_ERROR:
				failedReason = "ERROR_HTTP_DATA_ERROR";
				break;
			case DownloadManager.ERROR_INSUFFICIENT_SPACE:
				failedReason = "ERROR_INSUFFICIENT_SPACE";
				break;
			case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
				failedReason = "ERROR_TOO_MANY_REDIRECTS";
				break;
			case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
				failedReason = "ERROR_UNHANDLED_HTTP_CODE";
				break;
			case DownloadManager.ERROR_UNKNOWN:
				failedReason = "ERROR_UNKNOWN";
				break;
			}
			Log.d(TAG,"FAILED: " + failedReason);
			Toast.makeText(context,
					"FAILED: " + failedReason,
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_PAUSED:
			String pausedReason = "";

			switch(reason){
			case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
				pausedReason = "PAUSED_QUEUED_FOR_WIFI";
				break;
			case DownloadManager.PAUSED_UNKNOWN:
				pausedReason = "PAUSED_UNKNOWN";
				break;
			case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
				pausedReason = "PAUSED_WAITING_FOR_NETWORK";
				break;
			case DownloadManager.PAUSED_WAITING_TO_RETRY:
				pausedReason = "PAUSED_WAITING_TO_RETRY";
				break;
			}
			Log.d(TAG,"PAUSED: " + pausedReason);
			Toast.makeText(context,
					"PAUSED: " + pausedReason,
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_PENDING:
			//		Toast.makeText(this,
			//				"PENDING",
			//				Toast.LENGTH_LONG).show();
			Log.d(TAG,"Download started");
			Toast.makeText(context,
					"Download started",
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_RUNNING:
			Log.d(TAG,"Download running");
			Toast.makeText(context,
					"RUNNING",
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_SUCCESSFUL:
			Log.d(TAG,"Download done");
			Toast.makeText(context,
					"SUCCESSFUL",
					Toast.LENGTH_LONG).show();
			break;
		default:
			Log.d(TAG,"WTF "+status);
			Toast.makeText(context,
					"WTF"+status,
					Toast.LENGTH_LONG).show();
			break;
		}
	}
}

