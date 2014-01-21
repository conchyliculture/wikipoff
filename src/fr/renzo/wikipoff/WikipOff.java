package fr.renzo.wikipoff;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class WikipOff extends Application {

    private static final String TAG = "WikipOff";
    public static SharedPreferences config;
    private Context context ;
	public File DBDir;
	public Database dbHandler;
	
    public void onCreate(){
    	super.onCreate();
    	
    	context=getApplicationContext();
    	DBDir= new File(Environment.getExternalStorageDirectory(),getApplicationContext().getString(R.string.DBDir));
    	config = PreferenceManager.getDefaultSharedPreferences(this);  	
    	createEnv();
    }
    
	private void createEnv() {
		createDir(DBDir);
	}
	
	private void createDir(File f) {
		if (!f.exists()) {
			f.mkdirs();
		}
	}
}