package fr.renzo.pocketwiki;


import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Configg  {
	
	public SharedPreferences prefs;
	
	private static final String TAG = "Config";

	public static final String LASTUSEDDB="lastUsedDb";

	
	public String appRootDirPath;
	public String dbDirPath;
	public String lastUsedDb="";

	public ArrayList<String> availableDbFiles= new ArrayList<String>();

	private Context context;

	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public Configg (Context context){
		this.context = context;
		boolean iswrite = isExternalStorageWritable();
		if (!iswrite) {
			Log.d(TAG, "not fucking writeable");
		}
				
		// Set root env
		
		//store("appRootDir", appRootDirPath);
		// Set DB dir
		this.dbDirPath = appRootDirPath+ "/databases/";
		//store("dbDir", dbDirPath);
		// Set default DB
		this.lastUsedDb = this.prefs.getString(Configg.LASTUSEDDB, null);
		Log.d(TAG,"La last used db est "+this.lastUsedDb);
	}
	
	public void store (String key,Object val) {
		if (val instanceof String) {
			prefs.edit().putString(key, (String) val).commit();
		} else if (val instanceof Integer) {
			prefs.edit().putInt(key, (Integer) val).commit();
		} else {
			Log.e(TAG,"Type not supported for storing in prefs :"+val.getClass().getName());
		}
	
	}

	
	
	
}
