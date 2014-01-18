package fr.renzo.wikipoff;

import java.io.File;
import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
	
	private SharedPreferences config;
	private Context context;
	private File rootDbDir;
    private static final String TAG = "SettingsActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this.getApplicationContext();
        rootDbDir= new File(Environment.getExternalStorageDirectory(),s(R.string.DBDir));
        addPreferencesFromResource(R.xml.preferences);
        config = PreferenceManager.getDefaultSharedPreferences(this);
		 ListPreference lp =(ListPreference) findPreference(context.getString(R.string.database_select_pref));
		 String[] avDB = getAvailableDb();
		 if (avDB.length ==0) {
			 String[] msg = {"Please install some .sqlite files in"+rootDbDir.getAbsolutePath()};
			 avDB= msg;
		 }
		 lp.setEntries(avDB);
		 lp.setEntryValues(avDB);
		 lp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String clicked= (String) ((ListPreference)preference).getEntry();
				config.edit().putString(s(R.string.config_key_selecteddbfile),clicked ).commit();
				return true;
			}
		});	 
    }
    
   
    private String[] getAvailableDb() {
    	
    	HashSet<String> set = (HashSet<String>) config.getStringSet(s(R.string.config_key_available_database), new HashSet<String>());
    
    	if (set.size()==0) {
	    	for (File f : rootDbDir.listFiles()) {
	    		String name = f.getName();
	    		if (name.endsWith(".sqlite"))
	    			set.add(f.getName());
			}		
    	}
		return (String[]) set.toArray(new String[set.size()]);
	}

	  private  String s(int i) {
	    	return this.getString(i);
	    }

}