package fr.renzo.pocketwiki;

import java.io.File;
import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class PocketWiki extends Application {

    private static final String TAG = "PocketWiki";
    public static SharedPreferences config;
	
	//public File rootDir= Environment.getExternalStoragePublicDirectory("toto") ;
    private Context context ;
	private File DBDir;
	public ArrayList<String> availableDbFiles;
	
	public Database dbHandler;
	
    public void onCreate(){
    	super.onCreate();
    	context=getApplicationContext();
    	DBDir= new File(Environment.getExternalStorageDirectory(),getApplicationContext().getString(R.string.DBDir));
    	config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
    	
    	createEnv();
    	
    	loadAvailableDatabases(DBDir);
    	
    	Editor e = config.edit();
    	
    	if (!config.contains("selecteddbfile")) {
    		if (this.availableDbFiles==null) {
    			Toast.makeText(this, "Can't find any database in "+DBDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
    		} else {
    			if (this.availableDbFiles.size()>=1) {
    			e.putString(s(R.string.config_key_selecteddbfile), this.availableDbFiles.get(0));
    			}
    		}
    	}
    	
    	e.commit();
    	
    	File selectedDb=new File(DBDir,getConfigString(R.string.config_key_selecteddbfile));
    	dbHandler=new Database(context,selectedDb);
    }
    
    public  void updateAvailableDatabases(){
    	loadAvailableDatabases(DBDir);
    }
    
    public void loadAvailableDatabases(File dir) {
		ArrayList<String> results = new ArrayList<String>();
		if (! dir.isDirectory()) {
			Log.e(TAG, dir.getAbsolutePath()+" is not a directory!!");
			createDir(dir);
			return;
		} else {
	    	for (File f : dir.listFiles()) {
	    		String name = f.getName();
	    		if (name.endsWith(".sqlite"))
	    			results.add(f.getName());
			}
		}
		this.availableDbFiles=results;
		return ;
	}
    
    public  String s(int i) {
    	return context.getString(i);
    }
    
    private void createDir(File f) {
   // 	File f = new File(rootDir,path);
		if (!f.exists()) {
			f.mkdirs();
		}
    }
    
	private void createEnv() {
		// we need the db dir
		createDir(DBDir);
	}
	public void setConfigString(int i, String val) {
		config.edit().putString(s(i), val).commit();
	}
	public String getConfigString(int i) {
		return this.config.getString(s(i), null);
	}
	public String getConfigString(int i, String def) {
		String s = this.config.getString(s(i), def);
		Log.d(TAG,"Asking for config String for "+s(i)+" = '"+s+"'");
		return s;
				
	}

	public void updateDbHandler() {
		this.dbHandler = new Database(context,new File(DBDir,getConfigString(R.string.config_key_selecteddbfile) ));
	}





}