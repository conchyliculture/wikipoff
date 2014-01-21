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
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.Toast;
import fr.renzo.wikipoff.Database.DatabaseException;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private WikipOff app;
    private AutoCompleteTextView searchtextview;
    private ListView randomlistview;
	private Context context=this;
	private SharedPreferences config;

	public class SearchButtonClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if (searchtextview.getText().length()>0) {
			Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
			myIntent.putExtra("article_title", searchtextview.getText().toString()); 
			MainActivity.this.startActivity(myIntent);
			}
		}
	}
	
	public class SearchViewOnClickListener implements OnClickListener{
		private Context context;

		public SearchViewOnClickListener(Context context){
			super();
			this.context=context;
		}
		
		@Override
		public void onClick(View v) {
			Cursor tc;
			try {
				tc = app.dbHandler.getAllTitles();
				final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this.context,android.R.layout.simple_list_item_1, tc, new String[] {"title"},new int[] {android.R.id.text1},0);
				adapter.setFilterQueryProvider(new FilterQueryProvider() {
					public Cursor runQuery(CharSequence constraint) {
						Cursor c=null;
						try {
							if (constraint == null) {
								c= app.dbHandler.getAllTitles();
							} else {
								String s = '%' + constraint.toString() + '%';
								c= app.dbHandler.getAllTitles(s);
							} 
						} catch (DatabaseException e) {e.alertUser(context);}
						return c;
					}
				 });

				adapter.setCursorToStringConverter( new CursorToStringConverter() {
				@Override
					public CharSequence convertToString(Cursor cursor) {
						return cursor.getString(1);
					}
				});
				searchtextview.setAdapter(new SearchCursorAdapter(context, null, app.dbHandler));
			} catch (DatabaseException e) {
				e.alertUser(context);
			}
		}
	}
	
	public class ClearSearchClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			searchtextview.setText("");
		}
	}
	
	public class RandomSelectedClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
			myIntent.putExtra("article_title",  (String) randomlistview.getItemAtPosition(position));
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

		try {
			newDatabaseSelected();
		} catch (DatabaseException e) {
			Builder b = e.alertUser(context);
			b.setCancelable(false);
			b.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}  //onCancel
			}); //setOnCancelListener
		}
		
		ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
		searchButton.setOnClickListener(new SearchButtonClickListener());
		
		ImageButton clearSearchButton = (ImageButton) findViewById(R.id.clear_search_button);
		clearSearchButton.setOnClickListener(new ClearSearchClickListener());
		
		randomlistview= (ListView) findViewById(R.id.randomView);
		randomlistview.setOnItemClickListener(new RandomSelectedClickListener());			
		
		Button rndbutton = (Button) findViewById(R.id.buttonRandom);
		 
		rndbutton.setOnClickListener(new ShowRandomClickListener());
		
		searchtextview = (AutoCompleteTextView) findViewById(R.id.searchField);
		searchtextview.setOnClickListener(new SearchViewOnClickListener(context));
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		try {
			newDatabaseSelected();
		} catch (DatabaseException e) {
			e.alertUser(context);
			finish(); 
		}
	};

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
	
	private void newDatabaseSelected() throws DatabaseException {
		String seldb = config.getString(s(R.string.config_key_selecteddbfile),"" );
		if (seldb!="") {
			File dbfile = new File(app.DBDir,seldb);
				app.dbHandler = new Database(context,dbfile);
		} else {
			Toast.makeText(getApplicationContext(), "You need to select a database", 
					   Toast.LENGTH_LONG).show();
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		}
		Log.d(TAG,"We selected db '"+seldb+"'");
	}
	
	private  String s(int i) {
	    return context.getString(i);
	}
}
