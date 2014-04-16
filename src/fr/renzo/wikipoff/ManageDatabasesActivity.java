package fr.renzo.wikipoff;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class ManageDatabasesActivity extends TabActivity {


	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_databases);
		
		TabHost tabHost = getTabHost(); 
		
		Intent intent1 = new Intent().setClass(this, TabInstalledActivity.class);
		TabSpec tabSpec1 = tabHost
		  .newTabSpec("Tab1")
		  .setIndicator("Tab 1")
		  .setContent(intent1);
		Intent intent2 = new Intent().setClass(this, TabAvailableActivity.class);
		TabSpec tabSpec2 = tabHost
		  .newTabSpec("Tab2")
		  .setIndicator("Tab 2")
		  .setContent(intent2);
		Intent intent3 = new Intent().setClass(this, TabCustomActivity.class);
		TabSpec tabSpec3 = tabHost
		  .newTabSpec("Tab3")
		  .setIndicator("Tab 3")
		  .setContent(intent3);
		
		tabHost.addTab(tabSpec1);
		tabHost.addTab(tabSpec2);
		tabHost.addTab(tabSpec3); 
		tabHost.setCurrentTab(0);

	}

}

