package fr.renzo.wikipoff;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class Wiki {
	private static final String TAG = "WIKI";
	private String type;
	private String lang;
	private String url;
	private String gendate;
	private String version;
	private SQLiteDatabase sqlh;
	public String getType() {
		return type;
	}
	public String toString(){
		return this.type+" "+this.lang+" "+this.gendate;
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

	public String getGendate() {
		return gendate;
	}

	public void setGendate(String gendate) {
		this.gendate = gendate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Wiki () {
		
	}
	public File isAlreadyInstalled(Context context) {
		File rootDbDir= new File(Environment.getExternalStorageDirectory(),context.getString(R.string.DBDir));
		for (File f : rootDbDir.listFiles()) {
    	//	String name = f.getName();
    		if (f.getName().endsWith(".sqlite")){
    			this.sqlh = SQLiteDatabase.openDatabase(f.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS );
    			Cursor c;
				c = sqlh.rawQuery("SELECT * FROM metadata", new String[0]);
				if (c.moveToFirst()) {
		            do {
		                String k = c.getString(0);
		                String v = c.getString(1);
		                Log.d(TAG,k+":"+v);
		                if (k.equals("lang")) {
		                	if (! v.equals(this.lang)){
		                		Log.d(TAG,"Not okay: "+f.getAbsolutePath());
			                	Log.d(TAG,"lang: "+v+"should be: "+this.lang);
			                	break;
		                	}
		                }
		                if (k.equals("type")) {
		                	if (! v.equals(this.type)){
		                		Log.d(TAG,"Not okay: "+f.getAbsolutePath());
			                	Log.d(TAG,"type: "+v+"should be: "+this.type);
			                	break;
		                	}
		                }
		                if (k.equals("date")) {
		                	if (! v.equals(this.gendate)){
		                		Log.d(TAG,"Not okay: "+f.getAbsolutePath());
			                	Log.d(TAG,"date:"+v+"should be: "+this.gendate);
			                	break;
		                	}
		                }
		                if (k.equals("version")) {
		                	if (! v.equals(this.version)){
		                		Log.d(TAG,"Not okay: "+f.getAbsolutePath());
			                	Log.d(TAG,"version: "+v+"should be: "+this.version);
			                	break;
		                	}
		                }
		                return f;
		            } while (c.moveToNext());
		            
		        }
    		}
		}
		return null;
	}
}