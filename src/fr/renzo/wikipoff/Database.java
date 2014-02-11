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
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

public class Database   {
	private static final String TAG = "Database";
	public File seldatabasefile;
	private Context context;
	public SQLiteDatabase sqlh;
	public String lang;
	private long maxId;
	
	public Database(Context context, File databasefile) throws DatabaseException {
	        this.context=context;
	        this.seldatabasefile = databasefile;
	        String error=checkDatabaseHealth();
	        if ( !error.equals("")) {
	        	Log.e(TAG,"Error: "+error);
	        	throw (new DatabaseException(error));
	        }
			try {
				SQLiteDatabase sqlh = SQLiteDatabase.openDatabase(this.seldatabasefile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS );
				this.sqlh=sqlh;
			} catch (SQLiteCantOpenDatabaseException e) {
    			Toast.makeText(context, "Problem opening database '"+databasefile+"'"+e.getMessage(), Toast.LENGTH_LONG).show();
			} 
			this.maxId=getMaxId();
			this.lang=getDbLang();
	}
	
	private String getDbLang() throws DatabaseException {
		Cursor c= myRawQuery("SELECT value FROM metadata WHERE key ='lang'");
		if (c.moveToFirst()){
			return c.getString(0);
		}
		c.close();
		return "";
	}

	public String checkDatabaseHealth(){
		String error="";
		String p = seldatabasefile.getAbsolutePath();
		if (seldatabasefile==null) {
			return "I need a database file....";
		}
		if (!seldatabasefile.exists()) {
			return "Unable to find '"+p+"'";
		}
		if (seldatabasefile.length()==0) {
			return "Database file '"+p+"' is an empty file";
		} 
		return error;
	}
	
	public Cursor myRawQuery(String query) throws DatabaseException {
		return myRawQuery(query,new String[0]);
	}
	public Cursor myRawQuery(String query, String param1) throws DatabaseException {
		return myRawQuery(query,new String[]{param1});
	}
	
	public Cursor myRawQuery(String query, String[] objects ) throws DatabaseException {
		Log.d(TAG,"SQL: "+query);
		for (int i = 0; i < objects.length; i++) {
			Log.d(TAG,"SQL: "+objects[i]);
		}
		Cursor c=null;
		try {
			c = this.sqlh.rawQuery(query, objects);		
			
		} catch (SQLiteException e) {
			if (c!= null){
				c.close();
			}
			throw new DatabaseException(e.getMessage());
		}
		return c;
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
            
        } else {
        	Log.d(TAG,"What");
        }
		c.close();
		return res;
	}

	public List<String> getRandomTitles() throws DatabaseException {
		int nb=context.getResources().getInteger(R.integer.def_random_list_nb);
		return getRandomTitles(nb);
	}
	
	// This gets an existing article from an existing titne and won't try weird stuff
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
		//Log.d(TAG,"getarticlefromtitle '"+title+"' "+redirect);
		
		Article res=null;
		try {
			// First, let's try the easy way
			res=getArticleFromTitle(title);
			if (res==null) {
				// *sigh* maybe there's a redirect
				String redirtitle = getRedirectArticleTitle(title);
        		if (redirtitle!=null) {
        			Log.d(TAG, "ah, we found a redirect => "+redirtitle);
        			res=getArticleFromTitle(Html.fromHtml(redirtitle).toString());
        		}
        		if (res==null) {
        			// WTF who failed that much?
        			// Maybe just a case-sensitivity issue
        			Cursor c;
        			c=myRawQuery("SELECT title FROM searchTitles WHERE title match ?",title);
					if (c.moveToFirst()) {
						res= getArticleFromTitle(c.getString(0));
					} else {
						Log.d(TAG, "allezzzzz");
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
		try {
			c = myRawQuery("SELECT title_to FROM redirects WHERE title_from= ? or title_from =?", new String[]{title, uppertitle});
			if (c.moveToFirst()) {
	            String res=c.getString(0);
	            return res;
	        } else {
	        	Log.d(TAG,"No redirect found for title '"+title+"'");
	        }
			c.close();
		} catch (DatabaseException e) {
			e.alertUser(context);
		}
		return null;
	}


	public class DatabaseException extends Exception {

		private static final long serialVersionUID = -4015796136387495698L;
		public DatabaseException(String message) {
			super(message);
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
}
