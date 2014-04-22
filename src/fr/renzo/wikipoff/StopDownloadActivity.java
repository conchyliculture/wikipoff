package fr.renzo.wikipoff;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StopDownloadActivity extends Activity {
	
	private static final String TAG = "StopDownloadActivity";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Intent inputintent = getIntent();
		/* Unfortunately We can't use any of this, because of its non-workyness */
//		final long dlid = inputintent.getLongExtra("dlid", -1);
//		final long[] dlids = inputintent.getLongArrayExtra("dlids");
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
				//				if (dlid != -1) {
				//					stopDownload(dlid);
				//				}
				//				for (int i = 0; i < dlids.length; i++) {
				//					stopDownload(i);
				//				}
				stopDownload(0);  //whatevs; we won't use the id because DownloadManager is unreliable

				byebye();
			}
		});


	}
	private void byebye(){
		this.finish();
	}

	public void stopDownload(long downloadId){
		Log.d(TAG,"got id "+downloadId);
		DownloadManager dm  = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		/* Earlier unworky-ness means wel'ss just delete the current download
		 * Hopeing it's the correct one ... */
		DownloadManager.Query query = new DownloadManager.Query();

		Cursor cursor = null;
		cursor = dm.query(query);
		if (cursor.moveToFirst()) {
			String[] s = cursor.getColumnNames();
			for (int i = 0; i < s.length; i++) {
				if (s[i].equals("_id")) {
					dm.remove(cursor.getInt(i));
				}
			}
		}

		Intent outputintent = new Intent(this, ManageDatabasesActivity.class);
		outputintent.putExtra("command", "stopdownloads");
		outputintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(outputintent);
	}
}
