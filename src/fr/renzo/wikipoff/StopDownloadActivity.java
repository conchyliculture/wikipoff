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
		setTitle("Warning");
		setContentView(R.layout.alert_dialog);
		TextView msg = (TextView) findViewById(R.id.message);
		msg.setText("Are you sure you want to stop this download?");
		Intent inputintent = getIntent();
		final int position = inputintent.getIntExtra("position", -1);
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
				stopDownload(position); 
				byebye();
			}
		});


	}
	private void byebye(){
		this.finish();
	}

	public void stopDownload(int position){
		Intent outputintent = new Intent(this, ManageDatabasesActivity.class);
		outputintent.putExtra("command", "stopdownload");
		outputintent.putExtra("position", position);
		outputintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(outputintent);
	}
}
