package fr.renzo.wikipoff;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Wiki {
	private static final String TAG = "WIKI";
	private String type;
	private String lang;
	private String url;
	private String filename;
	private Date gendate;
	private String version;
	private long size;
	private Context context;
	
	public long getSize() {
		return size;
	}
	public String getSizeReadable(boolean si) {
		int unit = si ? 1000 : 1024;
	    if (size < unit) {
	    	return Long.toString(size) + " B";
	    }
	    int exp = (int) (Math.log(size) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", size / Math.pow(unit, exp), pre);
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	public String getType() {
		return type;
	}
	public String toString(){
		return this.type+" "+this.lang+" "+this.gendate;
	}
	
	public String getFilename(){
		return this.filename;
	}
	public void setFilename(String filename) {
		this.filename=filename;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getGendate() {
		return gendate;
	}

	public void setGendate(String gendate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			this.gendate = sdf.parse(gendate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Wiki (Context context) {
		this.context=context;
	}
	private SQLiteDatabase openDB(File sqlitefile) throws SQLiteDatabaseCorruptException {
		SQLiteDatabase sqlh=SQLiteDatabase.openDatabase(sqlitefile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS );
		return sqlh;
	}
	public Wiki (Context context,File sqlitefile) throws WikiException {
		setFilename(sqlitefile.getName());
		setSize(sqlitefile.length());
		this.context=context;
		if (sqlitefile.getName().endsWith(".sqlite")){
			SQLiteDatabase sqlh = openDB(sqlitefile);
			Cursor c;
			try {
			c = sqlh.rawQuery("SELECT * FROM metadata", new String[0]);
			if (c.moveToFirst()) {
	            do {
	                String k = c.getString(0);
	                String v = c.getString(1);
	                Log.d(TAG,k+":"+v);
	                if (k.equals("lang")) {
	                	setLang(v);
	                } else if (k.equals("type")) {
						setType(v);
					} else if (k.equals("date")) {
						setGendate(v);
					} else if (k.equals("version")) {
						setVersion(v);
					}
	            } while (c.moveToNext());
			}
			sqlh.close();
			} catch (SQLiteDatabaseCorruptException e) {
				throw new WikiException("Problem with database file: "+sqlitefile.getName()+". Please delete it!");
			}
			
		} else {
			Log.d(TAG,"not a sqlite file to load a Wiki from : "+sqlitefile);
		}
	}
	
	
	public File isAlreadyInstalled() throws WikiException {
		File rootDbDir= new File(Environment.getExternalStorageDirectory(),context.getString(R.string.DBDir));
		for (File sqlitefile : rootDbDir.listFiles()) {
			String sqlitefilename=sqlitefile.getName();
    		if (sqlitefilename.endsWith(".sqlite")){
				try {
    			SQLiteDatabase sqlh = openDB(sqlitefile);
				
    			Cursor c;
				c = sqlh.rawQuery("SELECT * FROM metadata", new String[0]);
				if (c.moveToFirst()) {
		            do {
		                String k = c.getString(0);
		                String v = c.getString(1);
		                Log.d(TAG,k+":"+v);
		                if (k.equals("lang")) {
		                	if (! v.equals(this.lang)){
		                		Log.d(TAG,"Not okay: "+sqlitefilename);
			                	Log.d(TAG,"lang: "+v+"should be: "+this.lang);
			                	break;
		                	}
		                }
		                if (k.equals("type")) {
		                	if (! v.equals(this.type)){
		                		Log.d(TAG,"Not okay: "+sqlitefilename);
			                	Log.d(TAG,"type: "+v+"should be: "+this.type);
			                	break;
		                	}
		                }
		                if (k.equals("date")) {
		                	if (! v.equals(this.gendate)){
		                		Log.d(TAG,"Not okay: "+sqlitefilename);
			                	Log.d(TAG,"date:"+v+"should be: "+this.gendate);
			                	break;
		                	}
		                }
		                if (k.equals("version")) {
		                	if (! v.equals(this.version)){
		                		Log.d(TAG,"Not okay: "+sqlitefilename);
			                	Log.d(TAG,"version: "+v+"should be: "+this.version);
			                	break;
		                	}
		                }
		                return sqlitefile;
		            } while (c.moveToNext());
		            
		        }
				sqlh.close();
    		} catch (SQLiteDatabaseCorruptException e) {
				throw new WikiException("Problem with database file: "+sqlitefilename+". Please delete it!");
			}
    		}
		}
		return null;
	}
	public boolean isSelected() {
		String key =context.getString(R.string.config_key_selecteddbfile);
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
		String sel_db=config.getString(key, "");
		if (sel_db.equals(getFilename())) {
			return true;
		} else {
			return false;
		}
	}
	public String getLocalizedGendate() {
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		return dateFormat.format(this.getGendate());
	}

}