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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Database   {
	private static final String TAG = "Database";
	public ArrayList<String> seldatabasefilespaths=new ArrayList<String>();
	private Context context;
	public ArrayList<SQLiteDatabase> sqlh=new ArrayList<SQLiteDatabase>();
	public String lang;
	private long maxId;

	public Database(Context context, ArrayList<String> databasefilespaths) throws DatabaseException {
		this.context=context;
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
		String storage= config.getString(context.getString(R.string.config_key_storage), StorageUtils.getDefaultStorage(context));
		File rootDbDir= new File(storage,context.getString(R.string.DBDir));

		for (String filename : databasefilespaths) {
			if (filename.equals("")) {
				continue;
			}
			this.seldatabasefilespaths.add(new File(rootDbDir,filename).getAbsolutePath());
		}


		String error = checkDatabaseHealth();
		if ( !error.equals("")) {
			throw (new DatabaseException(error));
		}


		for (String dbfile : this.seldatabasefilespaths) {
			try {
				SQLiteDatabase sqlh = SQLiteDatabase.openDatabase(dbfile, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS );
				this.sqlh.add(sqlh);
			}
			catch (SQLiteException e) {
				throw (new DatabaseException("Problem opening database '"+dbfile+"'"+e.getMessage()));
			} 
		}
		this.maxId=getMaxId();
		this.lang=getDbLang();
	}

	public Cursor myRawQuery(String query) throws DatabaseException {
		return myRawQuery(query,new String[0]);
	}

	public Cursor myRawQuery(String query, String param1) throws DatabaseException {
		return myRawQuery(query,new String[]{param1});
	}

	public Cursor myRawQuery(String query, String[] objects ) throws DatabaseException {
		Cursor[] arraycursors = new Cursor[this.sqlh.size()];
		int i=0;
		for (SQLiteDatabase sh : this.sqlh) {
			Cursor c=null;
			try {
				c = sh.rawQuery(query, objects);		
				arraycursors[i]=c;
				i+=1;
			} catch (SQLiteException e) {
				if (c!= null){
					c.close();
				}
				throw new DatabaseException(e.getMessage());
			}
		}
		return (Cursor) new MergeCursor(arraycursors);
	}

	private String getDbLang() throws DatabaseException {
		String res="";
		Cursor c = myRawQuery("SELECT value FROM metadata WHERE key ='lang-code'");
		if (c.moveToFirst()){
			res= c.getString(0);
		} else { 
			// patch for some old db
			c = myRawQuery("SELECT value FROM metadata WHERE key ='lang'");
			if (c.moveToFirst()){
				res= c.getString(0);
			} 
		}
		c.close();
		return res;
	}

	public String checkDatabaseHealth(){
		String error="";
		// TODO could add more checks
		for (String p : seldatabasefilespaths) {
			File dbfile = new File(p);
			if (!dbfile.exists()) {
				return "Unable to find '"+p+"'";
			}
			if (dbfile.length()==0) {
				return "Database file '"+p+"' is an empty file";
			} 
		}
		return error;
	}

	public int getMaxId() throws DatabaseException{
		Cursor c= myRawQuery("SELECT MAX(_id) FROM articles");
		if (c.moveToFirst()){
			return c.getInt(0);
		}
		c.close();
		return 0;
	}

	public List<String> getRandomTitles(int nb) throws DatabaseException {
		long[] rnd_ids = new long[nb];
		for (int i = 0; i < rnd_ids.length; i++) {
			rnd_ids[i]=0;
		}
		long max = this.maxId;

		int idx=0;
		while (rnd_ids[nb-1]==0) {
			boolean found=false;
			long ir = Math.round(Math.random()*max) + 1;
			for (int i = 0; i < idx+1; i++) {
				if (rnd_ids[i]==ir){
					found=true;
					break;
				}
			}
			if (!found){
				rnd_ids[idx]=ir;
				idx+=1;
			}
		}

		ArrayList<String> res = new ArrayList<String>();
		String q="("+rnd_ids[0];
		for (int i = 1; i < rnd_ids.length; i++) {
			q = q+", "+String.valueOf(rnd_ids[i]);
		}
		q = q +" )";

		Cursor c = myRawQuery("SELECT title FROM articles WHERE _id IN "+q);
		if (c.moveToFirst()) {
			do {
				String t = c.getString(0);
				res.add(t);
			} while (c.moveToNext());

		} 
		c.close();
		return res;
	}

	public List<String> getRandomTitles() throws DatabaseException {
		// TODO settings for R.integer.def_random_list_nb
		int nb=context.getResources().getInteger(R.integer.def_random_list_nb);
		return getRandomTitles(nb);
	}

	// This gets an existing article from an existing title and won't try weird stuff
	public Article getArticleFromTitle(String title) throws DatabaseException {
		Cursor c;
		Article res=null;
		String uppertitle=title.substring(0, 1).toUpperCase() + title.substring(1);
		c = myRawQuery("SELECT _id,text FROM articles WHERE title= ? or title =?",new String[]{title,uppertitle});
		if (c.moveToFirst()) {
			res = new Article(c.getInt(0),title,c.getBlob(1));
		}
		return res;
	}


	public Article searchArticleFromTitle(String title) {
		Article res=null;
		try {
			// First, let's try the easy way
			res=getArticleFromTitle(title);
			if (res==null) {
				// *sigh* maybe there's a redirect
				String redirtitle = getRedirectArticleTitle(title);
				if (redirtitle!="") {
					res=getArticleFromTitle(Html.fromHtml(redirtitle).toString());
				}
				if (res==null) {
					// WTF who failed that much?
					// Maybe just a case-sensitivity issue
					Cursor c;
					c = myRawQuery("SELECT title FROM searchTitles WHERE title match ?",title);
					if (c.moveToFirst()) {
						res = getArticleFromTitle(c.getString(0));
					} 
					c.close();
				}
			} 
		} catch (DatabaseException e) {
			e.alertUser(context);
		}
		if (res==null) {
			Log.d(TAG,"No article found for title '"+title+"'");
		}
		return res;
	}

	public String getRedirectArticleTitle(String title) {
		String uppertitle=title.substring(0, 1).toUpperCase() + title.substring(1);
		Cursor c;
		String res="";
		try {
			c = myRawQuery("SELECT title_to FROM redirects WHERE title_from= ? or title_from =?", new String[]{title, uppertitle});
			if (c.moveToFirst()) {
				res = c.getString(0);
			} else {
				Log.d(TAG,"No redirect found for title '"+title+"'");
			}
			c.close();
		} catch (DatabaseException e) {
			e.alertUser(context);
		}
		return res;
	}

	public class DatabaseException extends Exception {

		private static final long serialVersionUID = -4015796136387495698L;
		private static final String TAG="DatabaseException";
		public DatabaseException(String message) {
			super(message);
			Log.e(TAG,"Error: "+message);
		}

		public Builder alertUser(Context context){
			AlertDialog.Builder dialog = new AlertDialog.Builder(context); 
			dialog.setTitle("Database Error:");
			dialog.setMessage(this.toString());
			dialog.setNeutralButton("Ok", null);
			dialog.create().show();
			return dialog;
		}
	}

	public void close() {
		for (SQLiteDatabase db : this.sqlh) {
			db.close();
		}
	}
}
