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
package fr.renzo.wikipoff.ui.activities;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import fr.renzo.wikipoff.Database;
import fr.renzo.wikipoff.Database.DatabaseException;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.SearchCursorAdapter;
import fr.renzo.wikipoff.StorageUtils;
import fr.renzo.wikipoff.WikipOff;

public class MainActivity extends SherlockActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "MainActivity";
	private WikipOff app;
	private AutoCompleteTextView searchtextview;
	private ListView randomlistview;
	private Context context=this;
	private SharedPreferences config;
	private ImageButton clearSearchButton;
	private Button rndbutton;
	private File dbdir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.config=PreferenceManager.getDefaultSharedPreferences(this);
		this.app= (WikipOff) getApplication();

		setContentView(R.layout.activity_main);

		setStorage();

		clearSearchButton = (ImageButton) findViewById(R.id.clear_search_button);
		randomlistview= (ListView) findViewById(R.id.randomView);
		rndbutton = (Button) findViewById(R.id.buttonRandom);
		searchtextview = (AutoCompleteTextView) findViewById(R.id.searchField);

	}
	private void setStorage() {
		String storage_root_path = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage());
		dbdir= new File(storage_root_path,getString(R.string.DBDir));
		createEnv();
	}

	@Override
	public void onResume(){
		super.onResume();	
		setStorage();
		newDatabaseSelected();
		showViews();
	}

	private void showViews(){
		clearSearchButton.setOnClickListener(new ClearSearchClickListener());
		randomlistview.setOnItemClickListener(new RandomItemClickListener());			
		rndbutton.setOnClickListener(new ShowRandomClickListener());
		searchtextview.setAdapter(new SearchCursorAdapter(context, null, app.getDatabaseHandler(context)));
		searchtextview.setOnItemClickListener(new SearchClickListener());
		searchtextview.setOnEditorActionListener(new SearchClickListener());

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
				rndtitles = app.getDatabaseHandler(MainActivity.this).getRandomTitles();
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, rndtitles); 
				randomlistview.setAdapter(adapter);
			} catch (DatabaseException e) {e.alertUser(context);}
		}
	}
	private void createEnv() {
		if (! dbdir.exists()) {
			createDir(dbdir);
		}
	}

	private void createDir(File f) {
		if (!f.exists()) {
			f.mkdirs();
			if (!f.exists()) {
				Toast.makeText(this,getString(R.string.message_cant_create_in_storage, f.getAbsolutePath()) , Toast.LENGTH_LONG).show();
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i); 
			}
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.mainmenu, menu);
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
		Database dbHandler = app.getDatabaseHandler(this);
		if (dbHandler != null) {
			showViews();
			toggleAllViews(true);

		} else {
			toggleAllViews(false);
			clearViewData();
			Toast.makeText(getApplicationContext(), getString(R.string.message_no_selected_database), 
					Toast.LENGTH_LONG).show();
		}		
	}

	private void clearViewData() {
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter= ((ArrayAdapter<String>) this.randomlistview.getAdapter());
		if (adapter != null) {
			adapter.clear();
			adapter.notifyDataSetChanged();
		}
		this.searchtextview.setText("");
	}

	private void toggleAllViews(boolean state) {
		this.clearSearchButton.setEnabled(state);
		this.randomlistview.setEnabled(state);
		this.rndbutton.setEnabled(state);
		this.searchtextview.setEnabled(state);
	}

	private void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}
}
