package fr.renzo.wikipoff;



import android.app.Activity;
import android.content.Context;
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
import android.webkit.WebView;
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

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private WikipOff app;
    private AutoCompleteTextView searchtextview;
    private ListView randomlistview;
	private Context context=this;
	private SharedPreferences config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.config=PreferenceManager.getDefaultSharedPreferences(this);;

		this.app= (WikipOff) getApplication();
		setContentView(R.layout.activity_main);
		randomlistview= (ListView) findViewById(R.id.randomView);
	
		
		ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
		
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (searchtextview.getText().length()>0) {
				Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
				myIntent.putExtra("article_title", searchtextview.getText().toString()); //Optional parameters
				MainActivity.this.startActivity(myIntent);
				}
			}
		});
		
		ImageButton clearSearchButton = (ImageButton) findViewById(R.id.clear_search_button);
		clearSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchtextview.setText("");
			}
		});
		
		 
		 randomlistview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
				myIntent.putExtra("article_title",  (String) randomlistview.getItemAtPosition(position)); //Optional parameters
				MainActivity.this.startActivity(myIntent);
			}


		 });
		
		
		Button rndbutton = (Button) findViewById(R.id.buttonRandom);
		 
		rndbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int nb=10; //TODO
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, app.dbHandler.getRandomTitles(nb)); 
				randomlistview.setAdapter(adapter);
			}
		});
		
		if (app.dbHandler == null) {
			Toast.makeText(getApplicationContext(), "You need to select a database", 
					   Toast.LENGTH_LONG).show();
			Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
		    
		}else{
			searchtextview = (AutoCompleteTextView) findViewById(R.id.searchField);
			Cursor tc = app.dbHandler.getAllTitles();
			final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,
																tc, new String[] {"title"},new int[] {android.R.id.text1},0);
		
			adapter.setFilterQueryProvider(new FilterQueryProvider() {
				public Cursor runQuery(CharSequence constraint) {
					if (constraint == null) {
						return app.dbHandler.getAllTitles();
					}
					String s = '%' + constraint.toString() + '%';
					return app.dbHandler.getAllTitles(s);
				}
			 });

			adapter.setCursorToStringConverter( new CursorToStringConverter() {
			@Override
				public CharSequence convertToString(Cursor cursor) {
					return cursor.getString(1);
				}
			});
		 searchtextview.setAdapter(adapter);
		 
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		newDatabaseSelected();
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

	
	private void newDatabaseSelected() {
		Log.d(TAG,"We selected db '"+config.getString(s(R.string.config_key_selecteddbfile),"Pute?")+"'");
		this.app.updateDbHandler(); 
		
	}
	
	private  String s(int i) {
	    return context.getString(i);
	}
	
}
