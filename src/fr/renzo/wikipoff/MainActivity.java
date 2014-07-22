/*

Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
https://github.com/conchyliculture/wikipoff

This file is part of WikipOff.

    WikipOff is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WikipOff is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.renzo.wikipoff;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import fr.renzo.wikipoff.Database.DatabaseException;
import fr.renzo.wikipoff.StorageUtils.StorageInfo;

public class MainActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "MainActivity";
	private WikipOff app;
	private AutoCompleteTextView searchtextview;
	private ListView randomlistview;
	private Context context=this;
	private SharedPreferences config;
	private ArrayList<String> seldb;
	private ImageButton clearSearchButton;
	private Button rndbutton;
	private File dbdir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.config=PreferenceManager.getDefaultSharedPreferences(this);
		this.app= (WikipOff) getApplication();
		setContentView(R.layout.activity_main);
		
		String storage_root_path = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage());
				
		dbdir= new File(storage_root_path,getString(R.string.DBDir));
		if (savedInstanceState==null) {
			createEnv();
		}

		clearSearchButton = (ImageButton) findViewById(R.id.clear_search_button);
		randomlistview= (ListView) findViewById(R.id.randomView);
		rndbutton = (Button) findViewById(R.id.buttonRandom);
		searchtextview = (AutoCompleteTextView) findViewById(R.id.searchField);

	}

	@Override
	public void onResume(){
		super.onResume();	
		newDatabaseSelected();
		showViews();
	}

	public void showViews(){
		if (this.seldb != null && ! this.seldb.isEmpty()) {
			clearSearchButton.setOnClickListener(new ClearSearchClickListener());
			randomlistview.setOnItemClickListener(new RandomItemClickListener());			
			rndbutton.setOnClickListener(new ShowRandomClickListener());
			searchtextview.setAdapter(new SearchCursorAdapter(context, null, app.dbHandler));
			searchtextview.setOnItemClickListener(new SearchClickListener());
			searchtextview.setOnEditorActionListener(new SearchClickListener());

			toggleAllViews(true);
		} 
	}

	public class RandomItemClickListener implements OnItemClickListener {
		// Handles clicks on an item in the random article list
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
			myIntent.putExtra("article_title",  (String) randomlistview.getItemAtPosition(position));
			MainActivity.this.startActivity(myIntent);
		}
	}

	public class SearchClickListener implements OnItemClickListener, OnEditorActionListener {
		// Handles clicks on an item in the search article list
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Cursor c = (Cursor) parent.getItemAtPosition(position);
			displayArticle(c.getString(1));
		}

		private void displayArticle(String q){
			Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
			myIntent.putExtra("article_title",q );
			MainActivity.this.startActivity(myIntent);
		}

		@Override
		public boolean onEditorAction(TextView view, int arg1, KeyEvent arg2) {
			displayArticle(view.getText().toString());
			return true;
		}
	}

	public class ClearSearchClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			searchtextview.setText("");
		}
	}

	public class ShowRandomClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			hideSoftKeyboard();
			List<String> rndtitles;
			try {
				rndtitles = app.dbHandler.getRandomTitles();
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, rndtitles); 
				randomlistview.setAdapter(adapter);
			} catch (DatabaseException e) {e.alertUser(context);}
		}
	}
	private void createEnv() {
		createDir(dbdir);
	}

	private void createDir(File f) {
		boolean res;
		if (!f.exists()) {
			res= f.mkdirs();
			if (!res) {
				Toast.makeText(this, "Couldn't create "+f.getAbsolutePath()+". Please select an external storage", Toast.LENGTH_LONG).show();
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i); 
			}
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
		case R.id.action_manage_databases:
			Intent i1 = new Intent(this, ManageDatabasesActivity.class);
			startActivity(i1);
			return true;
		case R.id.action_about:
			Intent i2 = new Intent(this, AboutActivity.class);
			startActivity(i2);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void newDatabaseSelected() {
		try {
			String tosplit = config.getString(s(R.string.config_key_selecteddbfiles),null );


			if (tosplit!=null) {
				this.seldb = new ArrayList<String>(Arrays.asList(tosplit.split(",")));
				boolean new_db = config.getBoolean(s(R.string.config_key_should_update_db), false);
				if ((app.dbHandler == null ) || (new_db)){
					if (app.dbHandler!=null)
						app.dbHandler.close();
					app.dbHandler = new Database(context,this.seldb);
					config.edit().remove(s(R.string.config_key_should_update_db)).commit();
					showViews();
					toggleAllViews(true);
					//Log.d(TAG,"We selected db '"+seldb.toString()+"'");
				}
			} else {
				Toast.makeText(getApplicationContext(), "No selected database", 
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


	private void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}
}
