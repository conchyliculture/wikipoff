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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import fr.renzo.wikipoff.Database.DatabaseException;


public class ArticleActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "ArticleActivity";
	private Database dbHandler;
	private WebView webview;
	private Article article;
	private String wanted_title;
	private SharedPreferences config;
	private MenuItem searchItem;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		try {
			this.dbHandler= ((WikipOff) getApplication()).getDatabaseHandler();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
		this.config=PreferenceManager.getDefaultSharedPreferences(this);;

		setContentView(R.layout.activity_article);

		this.webview= (WebView) findViewById(R.id.article_webview);
		this.webview.getSettings().setJavaScriptEnabled(true);

		Intent source_intent = getIntent();
		wanted_title = source_intent.getStringExtra("article_title");
		this.article = dbHandler.searchArticleFromTitle(wanted_title);

		showHTML();
	}

	private void displayNewArticle(String title) {
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
			webIntent.setData( Uri.parse("http://"+dbHandler.lang+".wikipedia.org/wiki/"+this.article.title) );
			startActivity( webIntent );
			return true;
		case R.id.action_about:
			Intent i2 = new Intent(this, AboutActivity.class);
			startActivity(i2);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
		});

		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				webview.findNext(true);
				return true;
			}

			@SuppressWarnings("deprecation") // Because we want to work with API 14
			@Override
			public boolean onQueryTextChange(String newText) {
				webview.findAll(newText);
				return true;
			}
		});

		return true;
	}

	private void showHTML() {
		this.webview.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				String article_title=url;
				if (url.startsWith("file:///")) {
					article_title=url.substring(8);					
				}
				try {
					displayNewArticle(URLDecoder.decode(article_title, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return true;  
			} 
		});

		String data ="<html><head>\n";
		if (this.article != null) {
			setTitle(this.article.title);
			data+="<meta name=\"viewport\" content=\"width=device-width,  user-scalable=yes\">\n";
			data+="<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";
			data+="</head>";
			if (config.getBoolean(s(R.string.config_key_use_mathjax),true)) {
				data+="<script type=\"text/javascript\" src=\""+s(R.string.link_to_mathjax)+"\"></script>\n";
			}
			data+="<body>";
			data +="<h1>"+this.article.title+"</h1>";
			data += this.article.text;

		} else {
			data +=getString(R.string.html_message_no_article,wanted_title);
		}
		data+="</body></html>";
		this.webview.loadDataWithBaseURL("file:///android-assets", data, "text/html","UTF-8",null);
	}

	private  String s(int i) {
		return this.getString(i);
	}
}
