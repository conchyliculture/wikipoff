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
import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

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
//		 ListPreference lp =(ListPreference) findPreference(s(R.string.config_key_selecteddbfile));
//		 String[] avDB = getAvailableDb();
//		 if (avDB.length ==0) {
//			 String[] msg = {"Please install some .sqlite files in"+rootDbDir.getAbsolutePath()};
//			 avDB= msg;
//		 }
//		 lp.setEntries(avDB);
//		 lp.setEntryValues(avDB);
//		 lp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			
//			@Override
//			public boolean onPreferenceClick(Preference preference) {
//				String clicked= (String) ((ListPreference)preference).getValue();
//				config.edit().putString(s(R.string.config_key_selecteddbfile),clicked ).commit();
//				return true;
//			}
//		});	 
    }
   
//    private String[] getAvailableDb() {
//    	HashSet<String> set = (HashSet<String>) config.getStringSet(s(R.string.config_key_available_database), new HashSet<String>());
//    	if (set.size()==0) {
//	    	for (File f : rootDbDir.listFiles()) {
//	    		String name = f.getName();
//	    		if (name.endsWith(".sqlite"))
//	    			set.add(f.getName());
//			}		
//    	}
//		return (String[]) set.toArray(new String[set.size()]);
//	}
//
	private  String s(int i) {
		return context.getString(i);
	}
}
