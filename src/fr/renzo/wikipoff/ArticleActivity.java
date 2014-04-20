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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;


public class ArticleActivity extends Activity {

	private static final String TAG = "ArticleActivity";
	private WikipOff app;
	private WebView webview;
	private Article article;
	private String wanted_title;
	private SharedPreferences config;
	private MenuItem searchItem;
	
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
		wanted_title = source_intent.getStringExtra("article_title");
		this.article = app.dbHandler.searchArticleFromTitle(wanted_title);
				
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
		myIntent.putExtra("article_title", title);
		startActivity(myIntent);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_manage_databases:
			Intent ami = new Intent(this, ManageDatabasesActivity.class);
            startActivity(ami);
		    return true;
		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
		    return true;
		case R.id.action_webbrowser:
	        Intent webIntent = new Intent( Intent.ACTION_VIEW );
	        webIntent.setData( Uri.parse("http://"+app.dbHandler.lang+".wikipedia.org/wiki/"+this.article.title) );
	        this.startActivity( webIntent );
		
		 default:
		 return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.articlemenu, menu);
		searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (!hasFocus) {
		            searchItem.collapseActionView();
		        }
				
			}
		} );
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				webview.findNext(true);
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				webview.findAll(newText);
				return true;
			}
		});

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
			data +="<h1>No article '"+wanted_title+"' found =( </h1>";
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
