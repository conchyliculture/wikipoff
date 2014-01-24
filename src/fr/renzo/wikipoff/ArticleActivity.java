package fr.renzo.wikipoff;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.app.Activity;
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
	private Article article;
	private SharedPreferences config;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app= (WikipOff) getApplication();
		this.config=PreferenceManager.getDefaultSharedPreferences(this);;

		setContentView(R.layout.activity_article);
		
		this.webview= (WebView) findViewById(R.id.article_webview);
	    //this.webview.getSettings().setBuiltInZoomControls(true);
	    this.webview.getSettings().setJavaScriptEnabled(true);
		
		Intent source_intent = getIntent();
		int article_id = source_intent.getIntExtra("article_id",0);
		if (article_id==0){
			this.article = app.dbHandler.getArticleFromTitle(source_intent.getStringExtra("article_title"));
		} else {
			this.article = app.dbHandler.getArticleFromId(article_id);
		}
		showHTML();		
		
		this.webview.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// Catch clicks on links
//				Log.d(TAG,"Overriding"+ url);
				String article_title=url;
				if (url.startsWith("data:text/html")) {
					
				}else if (url.startsWith("file:///")) {
					article_title=url.substring(8);					
				}
				try {
					startArticleActivity(URLDecoder.decode(article_title, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	return true;  
		    } // method
		});// setWebViewClient
	} // onCreate
	
	private void startArticleActivity(String title) {
		Intent myIntent = new Intent(this, ArticleActivity.class);
		int newid = app.dbHandler.getArticleIdFromTitle(title);
		myIntent.putExtra("article_id", newid);
		startActivity(myIntent);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
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
	
	private void showHTML() {
//		Log.d(TAG, html.substring(0,10));
		String data ="<html><head>\n";
		if (this.article != null) {
			setTitle(this.article.title);
			data+="<meta name=\"viewport\" content=\"width=device-width,  user-scalable=yes\">\n";
			data+="</head>";
			if (config.getBoolean(s(R.string.config_key_use_mathjax),true)) {
				data+="<script type=\"text/javascript\" src=\""+s(R.string.link_to_mathjax)+"\"></script>\n";
			}
			data+="<body>";
			data +="<h1>"+this.article.title+"</h1>";
			data += this.article.text;
					
		} else {
			data +="<h1>No article found : =( </h1>";
		}
		data+="</body></html>";
		int len=300;
		if (data.length()<300)
			len=data.length();
		Log.d(TAG,data.substring(0,len));
		this.webview.loadDataWithBaseURL("file:///android-assets", data, "text/html; charset=UTF-8",null,null);
	}

	private  String s(int i) {
		return this.getString(i);
	}
}
