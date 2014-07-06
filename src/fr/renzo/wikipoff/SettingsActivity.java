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
import java.util.Iterator;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import fr.renzo.wikipoff.StorageUtils.StorageInfo;

public class SettingsActivity extends PreferenceActivity {
	private SharedPreferences config;
	private ListPreference myPref;
	@SuppressWarnings("unused")
	private static final String TAG = "SettingsActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		config = PreferenceManager.getDefaultSharedPreferences(this);

		List<StorageInfo> storagelist = StorageUtils.getStorageList();
		for (Iterator<StorageInfo> iterator = storagelist.iterator(); iterator.hasNext();) {
			StorageInfo storageInfo = iterator.next();
			if (!testWriteable(storageInfo.path)){
				storagelist.remove(storageInfo);
			}
		}
		
		String[] storage_names= new String[storagelist.size()];
		String[] storage_paths= new String[storagelist.size()];
		for (int i = 0; i < storagelist.size(); i++) {
			storage_names[i] = storagelist.get(i).getDisplayName();
			storage_paths[i] = storagelist.get(i).path;
		}
		
		myPref = (ListPreference) findPreference(getString(R.string.config_key_storage));
		myPref.setEntries(storage_names);
		myPref.setEntryValues(storage_paths);
		myPref.setSummary(config.getString(getString(R.string.config_key_storage), "Please select something"));
		myPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				myPref.setSummary((String)newValue);
				return true;
			}
		});
		
	}
	
	
	private boolean testWriteable(String path) {
		boolean res=false;
		File f = new File(path,".testdir");
		res = f.mkdirs();
		f.delete();
		return res;
	}
	
	



}
