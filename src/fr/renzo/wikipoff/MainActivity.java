package fr.renzo.wikipoff;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import fr.renzo.wikipoff.Database.DatabaseException;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private WikipOff app;
    private AutoCompleteTextView searchtextview;
    private ListView randomlistview;
	private Context context=this;
	private SharedPreferences config;
	private String seldb;
	private ImageButton clearSearchButton;
	private Button rndbutton;
	
	public class ClearSearchClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			searchtextview.setText("");
		}
	}
	
	@Override
	public void onRestart(){
		super.onRestart();	
		newDatabaseSelected();
	}
	
	public class RandomItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
			myIntent.putExtra("article_title",  (String) randomlistview.getItemAtPosition(position));
			MainActivity.this.startActivity(myIntent);
		}
	}
	public class SearchItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
			Cursor c = (Cursor) parent.getItemAtPosition(position);
			myIntent.putExtra("article_title", c.getString(1));
			MainActivity.this.startActivity(myIntent);
		}
	}
	
	public class ShowRandomClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {			
			List<String> rndtitles;
			try {
				rndtitles = app.dbHandler.getRandomTitles();
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, rndtitles); 
				randomlistview.setAdapter(adapter);
			} catch (DatabaseException e) {e.alertUser(context);}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.config=PreferenceManager.getDefaultSharedPreferences(this);
		this.app= (WikipOff) getApplication();
		setContentView(R.layout.activity_main);
		
		clearSearchButton = (ImageButton) findViewById(R.id.clear_search_button);
		randomlistview= (ListView) findViewById(R.id.randomView);
		rndbutton = (Button) findViewById(R.id.buttonRandom);
		searchtextview = (AutoCompleteTextView) findViewById(R.id.searchField);

		newDatabaseSelected();

		showViews();

	}
	
	public void showViews(){
		if (this.seldb != null) {
			clearSearchButton.setOnClickListener(new ClearSearchClickListener());
			randomlistview.setOnItemClickListener(new RandomItemClickListener());			
			rndbutton.setOnClickListener(new ShowRandomClickListener());
			searchtextview.setAdapter(new SearchCursorAdapter(context, null, app.dbHandler));
			searchtextview.setOnItemClickListener(new SearchItemClickListener());

			toggleAllViews(true);
		} 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
		    return true;
		  
		 default:
		 return super.onOptionsItemSelected(item);
		}
	}
	
	private void newDatabaseSelected() {
		try {
			this.seldb = config.getString(s(R.string.config_key_selecteddbfile),null );
			if (this.seldb!=null) {
				File dbfile = new File(app.DBDir,this.seldb);
				app.dbHandler = new Database(context,dbfile);
				showViews();
				toggleAllViews(true);
				Log.d(TAG,"We selected db '"+seldb+"'");
			} else {
				Toast.makeText(getApplicationContext(), "You need to select a database", 
						   Toast.LENGTH_LONG).show();
				toggleAllViews(false);
			}		
		} catch (DatabaseException e) {
			Builder b = e.alertUser(context);
			b.setCancelable(false);
			b.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}  //onCancel
			}); //setOnCancelListener
		} // try
	}
	
	private void toggleAllViews(boolean state) {
		this.clearSearchButton.setEnabled(state);
		this.randomlistview.setEnabled(state);
		this.rndbutton.setEnabled(state);
		this.searchtextview.setEnabled(state);
	}

	private  String s(int i) {
	    return context.getString(i);
	}
}
