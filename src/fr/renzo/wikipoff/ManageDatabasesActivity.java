package fr.renzo.wikipoff;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

public class ManageDatabasesActivity extends ActionBarActivity {
    public static final String TAG = "ManageDatabasesActivity";
	private DownloadManager downloadManager;
	private long downloadid;
		 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"created");
		setTitle("Manage your 'Wikis'");
		this.downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		setContentView(R.layout.activity_manage_databases);
		
		ActionBar bar = getSupportActionBar();
	    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    
	    ActionBar.Tab tabA = bar.newTab().setText("Downloaded Wikis");
	    ActionBar.Tab tabB = bar.newTab().setText("Available Wikis");

	    Fragment fragmentA = new TabInstalledFragment();
	    Fragment fragmentB = new TabAvailableFragment();

	    tabA.setTabListener(new MyTabsListener(fragmentA));
	    tabB.setTabListener(new MyTabsListener(fragmentB));

	    bar.addTab(tabA);
	    bar.addTab(tabB);

	}
	@Override
	protected void onResume() {
	 super.onResume();	 
	}

	
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG,"Got intent with action: "+action+" "+context.getPackageName());
		if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
			Log.d(TAG,"trying to stop");
			Intent i = new Intent(context.getApplicationContext(), ManageDatabasesActivity.class);
			i.putExtra("command", "stopdownload");
			context.startActivity(i);
		}
		checkDownloadStatus(context);
	}

    @Override
    protected void onNewIntent(Intent intent) {
    	Bundle extras = intent.getExtras();
    	if (extras != null) {
    		final String filename = extras.getString("filename");
    		final String url = extras.getString("url");
    		final String size = extras.getString("size");
    		new AlertDialog.Builder(this)
    		.setTitle("Warning")
    		.setMessage("Are you sure you want to download "+filename+" ("+size+")")
    		.setNegativeButton("No", null)
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				startDownload(filename, url);
    			}
    		})
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.show();
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
    
    public long do_download(String filename, String url){
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription("Downloading from "+url);
		request.setTitle(filename);
		request.setDestinationInExternalPublicDir(this.getString(R.string.DBDir), filename);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		this.downloadid = this.downloadManager.enqueue(request);
		
		Log.d(TAG,"pushed id "+this.downloadid);
        Query q = new Query();
        q.setFilterById(this.downloadid);
        return this.downloadid;
	}
    private void checkDownloadStatus(Context context){
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(this.downloadid);

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
//				Toast.makeText(this,
//						"PENDING",
//						Toast.LENGTH_LONG).show();
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
	private void startDownload(String filename, String url) {
		long dlid = do_download(filename,url);	
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
		config.edit().putLong(this.getString(R.string.config_key_downloadid),dlid ).commit();
	}
}

