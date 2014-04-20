package fr.renzo.wikipoff;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StopDownloadActivity extends Activity {
	
	private static final String TAG = "StopDownloadActivity";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Warning");
		setContentView(R.layout.alert_dialog);
		TextView msg = (TextView) findViewById(R.id.message);
		msg.setText("Are you sure you want to stop this download?");
		
		Button bno = (Button) findViewById(R.id.cancelbutton);
		bno.setText("no");
		bno.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				byebye();
			}
		});
		
		Button byes = (Button) findViewById(R.id.okbutton);
		byes.setText("yes");
		byes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopDownload();
				byebye();
			}
		});

	}
	private void byebye(){
		this.finish();
	}

	public int stopDownload(){
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
	    long downloadId = config.getLong(getString(R.string.config_key_downloadid), 0);
		Log.d(TAG,"got id "+downloadId);
	    DownloadManager dm  = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
	    int res = dm.remove(downloadId);
	    Log.d(TAG,"Removed "+res+" downloads");
	    return res;
	}
}
