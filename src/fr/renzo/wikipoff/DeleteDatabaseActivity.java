package fr.renzo.wikipoff;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DeleteDatabaseActivity extends Activity {
	
	@SuppressWarnings("unused")
	private static final String TAG = "DeleteDatabaseActivity";
	private ArrayList<String> dbtodelete;
	private int dbtodeleteposition;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i =getIntent();
		if (i.getExtras()!= null) {
			this.dbtodelete = i.getStringArrayListExtra("dbtodelete");
			this.dbtodeleteposition = i.getIntExtra("dbtodeleteposition", -1);
		}
		
		setTitle(getString(R.string.message_warning));
		setContentView(R.layout.alert_dialog);
		TextView msg = (TextView) findViewById(R.id.message);
		String txtmessage = getString(R.string.message_delete_db,dbtodelete.get(0));
		msg.setText(txtmessage);
		setResult(-1); //default is "we did nothing"
		Button bno = (Button) findViewById(R.id.cancelbutton);
		bno.setText(getString(R.string.no));
		bno.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				byebye();
			}
		});
		
		Button byes = (Button) findViewById(R.id.okbutton);
		byes.setText(getString(R.string.yes));
		byes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for(String db : dbtodelete)
					deleteDb(db);
				setResult(dbtodeleteposition);
				byebye();
			}
		});

	}

	public void deleteDb(String dbtodelete) {
		File db=new File(StorageUtils.getDBDirPath(this),dbtodelete);
		if (db.exists()) {
			db.delete();
		}
		
	}
	private void byebye(){
		
		this.finish();
	}
}
