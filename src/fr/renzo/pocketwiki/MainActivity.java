package fr.renzo.pocketwiki;


import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private PocketWiki app;
	private WebView webview;
    private AutoCompleteTextView searchtextview;
    private ListView randomlistview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app= (PocketWiki) getApplication();
		setContentView(R.layout.activity_main);
		randomlistview= (ListView) findViewById(R.id.randomView);

		
		searchtextview = (AutoCompleteTextView) findViewById(R.id.searchField);
		Cursor tc = app.dbHandler.getAllTitles();
		startManagingCursor(tc);
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

		
		if (searchtextview == null) {
			Log.d(TAG,"dafuk");
		} else {
			searchtextview.setAdapter(adapter);
		}
		
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
				// TODO Auto-generated method stub
				int nb=10;
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, app.dbHandler.getRandomTitles(nb)); 
				randomlistview.setAdapter(adapter);

			}
		});
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void selectDBPopup() {
		if (this.app == null) {
			this.app =(PocketWiki) getApplication();
		}
		this.app.updateAvailableDatabases();

		if (this.app.availableDbFiles == null) {
			Log.d(TAG,"No available DB Files");
		}
		final String[] available_db=(String[]) this.app.availableDbFiles.toArray(new String[this.app.availableDbFiles.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		int sel =-1;
		int i=0;
		String seldbname = this.app.dbHandler.seldatabasefile.getName();
		for (String a : available_db) {
			Log.d(TAG,"is '"+a+"' == '"+seldbname+"'");
			if (seldbname.equals(a)) {
				sel=i;
			}
			i+=1;			
		}

		// Add the buttons
		builder.setSingleChoiceItems(available_db, sel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
            	String dbname=(String) available_db[item];
                ((PocketWiki)getApplication()).setConfigString(R.string.config_key_selecteddbfile,dbname); 
       			Log.d(TAG,"Click on db '"+dbname+"'");
               }
            });
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.dismiss();
		               newDatabaseSelected();
		           }
		       });
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });

		AlertDialog dialog = builder.create();
		// Create the AlertDialog
		dialog.show();
		
		
		return ;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Log.d(TAG,"We selected dat menu "+String.valueOf(item.getItemId()));
	    switch (item.getItemId()) {
	    case R.id.action_set_database:
	        selectDBPopup();
	        
	        return true;
	    case R.id.action_settings:
	        // TODO
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

//	private void showRandomArticle() {
//		showHTML(this.app.dbHandler.getRandomArticle());
//	}
	
//	private void showHTML(String html) {
//		//Log.d(TAG, "<html><body>"+html+"</body></html>");
//		this.webview.loadData("<html><body>"+html+"</body></html>", "text/html; charset=UTF-8",null );
//	}
//	private void showArticle(String article) {
//		showHTML(this.app.dbHandler.getArticle(article));
//	}
//	
	
	private void newDatabaseSelected() {
		// TODO Auto-generated methodstub
		Log.d(TAG,"We selected db '"+app.getConfigString(R.string.config_key_selecteddbfile)+"'");
		this.app.updateDbHandler(); 
		
		//showHTML(this.app.dbHandler.getRandomArticle());
	}
	

	
}
