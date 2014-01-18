package fr.renzo.wikipoff;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class ArticleActivity extends Activity {

	private static final String TAG = "ArticleActivity";
	private WikipOff app;
	private WebView webview;
	private String article;
	private SharedPreferences config;
	

	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app= (WikipOff) getApplication();
		this.config=PreferenceManager.getDefaultSharedPreferences(this);;

		setContentView(R.layout.activity_article);

		this.webview= (WebView) findViewById(R.id.article_webview);
		//this.webview.getSettings().setUseWideViewPort(true); // comportement desktop?
	    this.webview.getSettings().setBuiltInZoomControls(true);
	    //this.webview.getSettings().setDisplayZoomControls(true);
	    //this.webview.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
	    // this.webview.getSettings().setLoadWithOverviewMode(true); // Load tout dezoomed
	    this.webview.getSettings().setJavaScriptEnabled(true);
	    
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("article_title");
		this.article = app.dbHandler.getArticle(title);
		showHTML(this.article,title);
		
		this.webview.setWebViewClient(new WebViewClient(){
			
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d(TAG,"Overriding"+ url);
				if (url.startsWith("data:text/html")) {
					
				}else {
					startArticleActivity(url);
				}
		    	return true;
		        
		    }
		});
		setTitle(intent.getStringExtra("article_title"));
	}
	
	private void startArticleActivity(String title) {
		Intent myIntent = new Intent(this, ArticleActivity.class);
		myIntent.putExtra("article_title", title); //Optional parameters
		startActivity(myIntent);
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	private void showHTML(String html,String title) {
		Log.d(TAG, html.substring(0,10));
		String data ="<html><head>\n";
		data+="<meta name=\"viewport\" content=\"width=device-width,  user-scalable=yes\">\n";
		data+="</head>";
		if (config.getBoolean(s(R.string.config_key_use_mathjax),true)) {
			data+="<script type=\"text/javascript\" src=\""+app.s(R.string.link_to_mathjax)+"\"></script>\n";
		}
		data+="<body>";
		data +="<h1>"+title+"</h1>";
		data = data+html+"</body></html>";
		Log.d(TAG,data.substring(0,300));
		this.webview.loadDataWithBaseURL("file:///android-assets", data, "text/html; charset=UTF-8",null,null);
	}

	  private  String s(int i) {
	    	return this.getString(i);
	    }
}
