package fr.renzo.pocketwiki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class ArticleActivity extends Activity {

	private static final String TAG = "ArticleActivity";
	private PocketWiki app;
	private WebView webview;
	private String article;

	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app= (PocketWiki) getApplication();
		setContentView(R.layout.activity_article);
//		Button rndbutton = (Button) findViewById(R.id.buttonRandom);

		this.webview= (WebView) findViewById(R.id.article_webview);
		//this.webview.getSettings().setUseWideViewPort(true); // comportement desktop?
	    this.webview.getSettings().setBuiltInZoomControls(true);
	    this.webview.getSettings().setDisplayZoomControls(true);
	    //this.webview.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
	    // this.webview.getSettings().setLoadWithOverviewMode(true); // Load tout dezoomed
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("article_title");
		this.article = app.dbHandler.getArticle(title);
		showHTML(this.article,title);
		
		this.webview.setWebViewClient(new WebViewClient(){
			
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d(TAG,"Overriding"+ url);
				if (url.startsWith("data:text/html")) {
					
				}else {
					Intent myIntent = new Intent(app.getApplicationContext(), ArticleActivity.class);
					myIntent.putExtra("article_title", url); //Optional parameters
					startActivity(myIntent);
				}
		    	return true;
		        
		    }
		});
		setTitle(intent.getStringExtra("article_title"));
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

		if (this.app.availableDbFiles == null) {
			Log.d(TAG,"No available DB Files");
		}
		final String[] available_db=(String[]) this.app.availableDbFiles.toArray(new String[this.app.availableDbFiles.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		int sel =-1;
		int i=0;
		for (String a : available_db) {
			Log.d(TAG,"is '"+a+"' == '"+this.app.dbHandler.seldatabasefile+"'");
			if (a.equals(this.app.dbHandler.seldatabasefile)) {
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

	
	private void showHTML(String html,String title) {
		//Log.d(TAG, html.substring(0,100));
		String data = String.format("<html><head><meta name=\"viewport\" content=\"width=device-width,  user-scalable=yes\"></head><body>");
		data +="<h1>"+title+"</h1>";
		data = data+html+"</body></html>";
		
		this.webview.loadData(data, "text/html; charset=UTF-8",null);
	}

	
	private void newDatabaseSelected() {
		Log.d(TAG,"We selected db '"+app.getConfigString(R.string.config_key_selecteddbfile)+"'");
		this.app.updateDbHandler(); 
		
	}
	
}
