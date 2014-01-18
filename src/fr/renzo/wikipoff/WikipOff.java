package fr.renzo.wikipoff;

import java.io.File;
import java.util.HashSet;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class WikipOff extends Application {

    private static final String TAG = "WikipOff";
    public static SharedPreferences config;
	
    private Context context ;
	private File DBDir;
	
	public Database dbHandler;
	
//	String seldb=config.getString(s(R.string.config_key_selecteddbfile),"");
//	if (seldb==""){
//		Log.e(TAG,"Shiiieeeeet ");
//        String[] avDB = getAvailableDb();
//        if (avDB.length==0) {
//        	shoutAtUser("Couldn't find any sqlite file in '"+"");
//        	
//        
//        }
//		
//	} else {
//	
//		File selectedDb=new File(DBDir,seldb);
//		dbHandler=new Database(context,selectedDb);
//	}
	
    public void onCreate(){
    	super.onCreate();
    	
    	context=getApplicationContext();
    	DBDir= new File(Environment.getExternalStorageDirectory(),getApplicationContext().getString(R.string.DBDir));
    	config = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	updateDbHandler();
    	createEnv();

    }

    public  String s(int i) {
    	return context.getString(i);
    }
    
	private void createEnv() {
		// we need the db dir
		createDir(DBDir);
	}
	
	 private void createDir(File f) {
			if (!f.exists()) {
				f.mkdirs();
			}
	    }

	public void updateDbHandler() {
		String seldb = config.getString(s(R.string.config_key_selecteddbfile),"" );
		if (seldb!="") {
			File dbfile = new File(DBDir,seldb);
			this.dbHandler = new Database(context,dbfile);
		}
	}
}