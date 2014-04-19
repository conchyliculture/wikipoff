package fr.renzo.wikipoff;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

public class ManageDatabasesActivity extends ActionBarActivity {
    public static final String TAG = "ManageDatabasesActivity";

	ViewPager mViewPager;
	
	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		 @Override
		 public void onReceive(Context context, Intent intent) {
			 checkDownloadStatus();}
		 };
		 
	private DownloadManager downloadManager;
	private long downloadid;
	
	private void checkDownloadStatus(){
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(downloadid);
		Cursor cursor = downloadManager.query(query);
		if(cursor.moveToFirst()){
			int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			int status = cursor.getInt(columnIndex);
			int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
			int reason = cursor.getInt(columnReason);
			
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

				Toast.makeText(this,
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

				Toast.makeText(this,
						"PAUSED: " + pausedReason,
						Toast.LENGTH_LONG).show();
				break;
			case DownloadManager.STATUS_PENDING:
				Toast.makeText(this,
						"PENDING",
						Toast.LENGTH_LONG).show();
				break;
			case DownloadManager.STATUS_RUNNING:
				Toast.makeText(this,
						"RUNNING",
						Toast.LENGTH_LONG).show();
				break;
			case DownloadManager.STATUS_SUCCESSFUL:

				Toast.makeText(this,
						"SUCCESSFUL",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	private void do_download(String filename, String url){
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription("Some descrition");
		request.setTitle("Some title");
		request.setDestinationInExternalPublicDir(this.getString(R.string.DBDir), filename);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		// get download service and enqueue file
		this.downloadid = downloadManager.enqueue(request);
        Query q = new Query();
        q.setFilterById(this.downloadid);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		setContentView(R.layout.activity_manage_databases);

		
		ActionBar bar = getSupportActionBar();
	    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    
	    ActionBar.Tab tabA = bar.newTab().setText("A Tab");
	    ActionBar.Tab tabB = bar.newTab().setText("B Tab");
	    ActionBar.Tab tabC = bar.newTab().setText("C Tab");

	    Fragment fragmentA = new TabInstalledFragment();
	    Fragment fragmentB = new TabAvailableFragment();
	    Fragment fragmentC = new TabCustomFragment();

	    tabA.setTabListener(new MyTabsListener(fragmentA));
	    tabB.setTabListener(new MyTabsListener(fragmentB));
	    tabC.setTabListener(new MyTabsListener(fragmentC));
	    bar.addTab(tabA);
	    bar.addTab(tabB);
	    bar.addTab(tabC);

		
//		Intent intent1 = new Intent().setClass(this, TabInstalledActivity.class);
//		TabSpec tabSpec1 = tabHost
//		  .newTabSpec("Tab1")
//		  .setIndicator("Installed Wikis")
//		  .setContent(intent1);
//		Intent intent2 = new Intent().setClass(this, TabAvailableActivity.class);
//		TabSpec tabSpec2 = tabHost
//		  .newTabSpec("Tab2")
//		  .setIndicator("Available Wikis")
//		  .setContent(intent2);
//		Intent intent3 = new Intent().setClass(this, TabCustomActivity.class);
//		TabSpec tabSpec3 = tabHost
//		  .newTabSpec("Tab3")
//		  .setIndicator("Custom Wikis")
//		  .setContent(intent3);
//		
//		tabHost.addTab(tabSpec1);
//		tabHost.addTab(tabSpec2);
//		tabHost.addTab(tabSpec3); 
//		tabHost.setCurrentTab(0);

	}
	@Override
	protected void onResume() {
	 super.onResume();

	 checkDownloadStatus();

	 IntentFilter intentFilter
	  = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
	 registerReceiver(downloadReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
	 super.onPause();
	 unregisterReceiver(downloadReceiver);
	}
    @Override
    protected void onNewIntent(Intent intent) {
       Bundle extras = intent.getExtras();
       if (extras != null) {
    	   do_download(extras.getString("filename"), extras.getString("url"));
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
            // some people needed this line as well to make it work: 
            ft.remove(fragment);
        }
    }
}

