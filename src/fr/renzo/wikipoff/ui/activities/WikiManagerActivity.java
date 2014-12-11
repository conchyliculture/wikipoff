package fr.renzo.wikipoff.ui.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.UUID;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.StorageUtils;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.WikiException;
import fr.renzo.wikipoff.WikipOff;
import fr.renzo.wikipoff.ui.fragments.FragmentInstalledTypes;
import fr.renzo.wikipoff.ui.fragments.WikiManagerAvailableFragment;

public class WikiManagerActivity extends SherlockFragmentActivity implements ActionBar.TabListener, OnQueryTextListener {

	private static final String TAG = "WikiManagerActivity";
	private WikipOff app;
	private String storage;
	
	// god bless https://gist.github.com/andreynovikov/4619215
	enum TabType
	{
		INSTALLED, AVAILABLE
	}
 
	// Tab back stacks
	private HashMap<TabType, Stack<String>> backStacks;
	private boolean refresh_installed_wikis;
	private ArrayList<Wiki> installed_wikis;
 
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
 
		this.app = (WikipOff) getApplication();
		SharedPreferences config= PreferenceManager.getDefaultSharedPreferences(this);
		this.storage = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage());

		setTitle(getString(R.string.title_manage_wikis));
		
		
		// Initialize ActionBar
		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
		// Set back stacks
		if (savedInstanceState != null)
		{
			// Read back stacks after orientation change
			backStacks = (HashMap<TabType, Stack<String>>) savedInstanceState.getSerializable("stacks");
		}
		else
		{
			// Initialize back stacks on first run
			backStacks = new HashMap<TabType, Stack<String>>();
			backStacks.put(TabType.INSTALLED, new Stack<String>());
			backStacks.put(TabType.AVAILABLE, new Stack<String>());
		}
		this.installed_wikis = getInstalledWikis();
		
		// Create tabs
		bar.addTab(bar.newTab().setTag(TabType.INSTALLED).setText("Installed").setTabListener(this));
		bar.addTab(bar.newTab().setTag(TabType.AVAILABLE).setText("Available").setTabListener(this));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.wikimanagermenu, menu);
		SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());

		searchView.setOnQueryTextListener(this);
		menu.add("Search")
		.setActionView(searchView)
		.setIcon(R.drawable.ic_action_search)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		return true;
	}
 
	@Override
	protected void onResume()
	{
		super.onResume();
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());
		if (! backStack.isEmpty())
		{
			// Restore topmost fragment (e.g. after application switch)
			String tag = backStack.peek();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			if (fragment.isDetached())
			{
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.attach(fragment);
				ft.commit();
			}
		}
	}
 
	@Override
	protected void onPause()
	{
		super.onPause();
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());
		if (! backStack.isEmpty())
		{
			// Detach topmost fragment otherwise it will not be correctly displayed
			// after orientation change
			String tag = backStack.peek();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			ft.detach(fragment);
			ft.commit();
		}
	}
 
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		// Restore selected tab
		int saved = savedInstanceState.getInt("tab", 0);
		if (saved != getSupportActionBar().getSelectedNavigationIndex())
			getSupportActionBar().setSelectedNavigationItem(saved);
	}
 
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		// Save selected tab and all back stacks
		outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
		outState.putSerializable("stacks", backStacks);
	}
 
	@Override
	public void onBackPressed()
	{
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());
		String tag = backStack.pop();
		if (backStack.isEmpty())
		{
			// Let application finish
			super.onBackPressed();
		}
		else
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			// Animate return to previous fragment
		//	ft.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
			// Remove topmost fragment from back stack and forget it
			ft.remove(fragment);
			showFragment(backStack, ft);
			ft.commit();
		}
	}
 
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// Select proper stack
		Stack<String> backStack = backStacks.get(tab.getTag());
		if (backStack.isEmpty())
		{
			// If it is empty instantiate and add initial tab fragment
			Fragment fragment;
			switch ((TabType) tab.getTag())
			{
				case INSTALLED:
					fragment = Fragment.instantiate(this, FragmentInstalledTypes.class.getName());
					break;
				case AVAILABLE:
					fragment = Fragment.instantiate(this, WikiManagerAvailableFragment.class.getName());
					break;
				default:
					throw new java.lang.IllegalArgumentException("Unknown tab");
			}
			addFragment(fragment, backStack, ft);
		}
		else
		{
			// Show topmost fragment
			showFragment(backStack, ft);
		}
	}
 
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
		// Select proper stack
		Stack<String> backStack = backStacks.get(tab.getTag());
		// Get topmost fragment
		String tag = backStack.peek();
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
		// Detach it
		ft.detach(fragment);
	}
 
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
		// Select proper stack
		Stack<String> backStack = backStacks.get(tab.getTag());
 
//		if (backStack.size() > 1)
//			ft.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
		// Clean the stack leaving only initial fragment
		while (backStack.size() > 1)
		{
			// Pop topmost fragment
			String tag = backStack.pop();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			// Remove it
			ft.remove(fragment);
		}
		showFragment(backStack, ft);
	}
 
	public void addFragment(Fragment fragment)
	{
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());
 
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// Animate transfer to new fragment
//		ft.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right);
		// Get topmost fragment
		String tag = backStack.peek();
		Fragment top = getSupportFragmentManager().findFragmentByTag(tag);
		ft.detach(top);
		// Add new fragment
		addFragment(fragment, backStack, ft);
		ft.commit();
	}
 
	private void addFragment(Fragment fragment, Stack<String> backStack, FragmentTransaction ft)
	{
		// Add fragment to back stack with unique tag
		String tag = UUID.randomUUID().toString();
		ft.add(android.R.id.content, fragment, tag);
		backStack.push(tag);
	}
 
	private void showFragment(Stack<String> backStack, FragmentTransaction ft)
	{
		// Peek topmost fragment from the stack
		String tag = backStack.peek();
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
		// and attach it
		ft.attach(fragment);		
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO
		return true;
	}

	@Override
	@SuppressWarnings("deprecation") // Because we want to work with API 14
	public boolean onQueryTextChange(String newText) {
		// TODO
		return true;
	}
	
	// Utils
	public ArrayList<Wiki> getInstalledWikis(){
		ArrayList<Wiki> res = new ArrayList<Wiki>();
		if (this.installed_wikis == null || this.refresh_installed_wikis) {
			HashMap<String, Wiki> multiwikis = new HashMap<String, Wiki>();

			Collection<String> currendl = getCurrentDownloads();
			for (File f : new File(getStorage(),getString(R.string.DBDir)).listFiles()) {
				if (! f.getName().endsWith(".sqlite")) {
					continue;
				}
				String name = f.getName();
				if (name.indexOf("-")>0) {
					String root_wiki=name.substring(0, name.indexOf("-"));
					if (multiwikis.containsKey(root_wiki)){
						Wiki w = multiwikis.get(root_wiki);
						w.addDBFile(f);
					} else {
						try {
							Wiki w = new Wiki(this, f);
							multiwikis.put(root_wiki,w);
						} catch (WikiException e) {
							Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
					}
				} else {
					try {
						if (! currendl.contains(f.getName())) {
							Wiki w = new Wiki(this,f);
							res.add(w);
						}
					} catch (WikiException e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			}
			for (Wiki w : multiwikis.values()) {
				if (! currendl.contains(w.getFilenamesAsString()))
					res.add(w);
			}

			Collections.sort(res, new Comparator<Wiki>() {
				public int compare(Wiki w1, Wiki w2) {
					if (w1.getLangcode().equals(w2.getLangcode())) {
						return w1.getGendateAsDate().compareTo(w2.getGendateAsDate());
					} else {
						return w1.getLangcode().compareToIgnoreCase(w2.getLangcode());
					}
				}
			}
					);
		} else {
			res= this.installed_wikis;
		}

		return res;
	}
	public Collection<String> getCurrentDownloads() {
		return (this.app.currentdownloads.values());
	}
	public String getStorage() {
		return this.storage;
	}

	public ArrayList<String> getWikiTypes() {
		ArrayList<String> res = new ArrayList<String>();

		for (Iterator<Wiki> iterator = getInstalledWikis().iterator(); iterator.hasNext();) {
			Wiki wiki = (Wiki) iterator.next();
			String wikitype = wiki.getType();
			if (!res.contains(wikitype)) {
				res.add(wikitype);
			}
		}
		return res;
	}
	public ArrayList<Wiki> getWikiByTypes(String type) {
		ArrayList<Wiki> res = new ArrayList<Wiki>();

		for (Iterator<Wiki> iterator = getInstalledWikis().iterator(); iterator.hasNext();) {
			Wiki wiki = (Wiki) iterator.next();
			if (wiki.getType().equals(type)) {
				res.add(wiki);
			} else {
				Log.d(TAG,wiki.getType()+" != "+type);
			}
		}
		return res;
	}


//	// The following code shows how to properly open new fragment. It assumes
//		// that parent fragment calls its activity via interface. This approach
//		// is described in Android development guidelines.
//		@Override
//		public void onItemSelected(String item)
//		{
//			ItemFragment fragment = new ItemFragment();
//			Bundle args = new Bundle();
//			args.putString("item", item);
//			fragment.setArguments(args);
//			addFragment(fragment);
//		}
	
//	@Override
//	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//		// TODO Auto-generated method stub
//		Log.d(TAG,"pute");
//		
//	}

//	@Override
//	public void onNothingSelected(AdapterView<?> parent) {
//		// TODO Auto-generated method stub
//		
//	}

	
}
