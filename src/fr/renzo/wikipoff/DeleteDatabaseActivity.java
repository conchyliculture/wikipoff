package fr.renzo.wikipoff;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DeleteDatabaseActivity extends Activity {
	
	@SuppressWarnings("unused")
	private static final String TAG = "DeleteDatabaseActivity";
	private ArrayList<String> dbtodelete;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i =getIntent();
		if (i.getExtras()!= null) {
			this.dbtodelete = i.getStringArrayListExtra("dbtodelete");
		}
		
		setTitle("Warning");
		setContentView(R.layout.alert_dialog);
		TextView msg = (TextView) findViewById(R.id.message);
		msg.setText("Are you sure you want to delete "+dbtodelete.get(0)+"?");
		
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
				for(String db : dbtodelete)
				deleteDb(db);
				byebye();
			}
		});

	}
	private void byebye(){
		this.finish();
	}

	public void deleteDb(String dbtodelete) {
		File db=new File(Environment.getExternalStoragePublicDirectory(this.getString(R.string.DBDir)),dbtodelete);
		if (db.exists()) {
			db.delete();
		}
	}
}
