package fr.renzo.wikipoff.ui.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;

public class WikiAvailableActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "WikiInstalledActivity";
	private Wiki wiki;
	private SharedPreferences config;
	private CheckedTextView iconview;
	private int position;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_installed_wiki);

		config = PreferenceManager.getDefaultSharedPreferences(this);

		Intent intent = getIntent();
		this.position = intent.getIntExtra("position", -1);
		this.wiki = (Wiki) intent.getExtras().getSerializable("wiki");
		// WARNING Wiki needs a context, it was lost on serializing...
		wiki.setContext(this);

		setTitle(wiki.getType()+" - "+wiki.getLangcode());

		iconview = (CheckedTextView) findViewById(R.id.wikiSelectCheckbox);

		setViews();
	}

	private void setViews() {
		setIcon();
		setSelected();
		setLanguage();
		setGenDate();
		setSource();
		setAuthor();
		setType();
		setSize();
		setFiles();
		setDelete();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == WikiManagerActivity.REQUEST_DELETE_CODE) {
			setResult(resultCode);
			finish();
		} 
	}

	private void setDelete() {
		Button b = (Button) findViewById(R.id.wikiDeleteButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent outputintent = new Intent(WikiAvailableActivity.this, DeleteDatabaseActivity.class);
				outputintent.putStringArrayListExtra("dbtodelete", wiki.getDBFilesnamesAsList());
				outputintent.putExtra("dbtodeleteposition", position);
				startActivityForResult(outputintent,ManageDatabasesActivity.REQUEST_DELETE_CODE);

			}
		});
	}

	private void setFiles() {
		TextView d = (TextView) findViewById(R.id.wikiFilesTextView);
		d.setText(wiki.getFilenamesAsString());
	}

	private void setSize() {
		TextView d = (TextView) findViewById(R.id.wikiSizeTextView);
		d.setText(wiki.getSizeReadable(true));
	}

	private void setType() {
		TextView d = (TextView) findViewById(R.id.wikiTypeTextView);
		d.setText(wiki.getType());
	}

	private void setLanguage() {
		TextView d = (TextView) findViewById(R.id.wikiLanguageTextView);
		d.setText(wiki.getLanglocal()+" / "+wiki.getLangcode());
	}

	private void setAuthor() {
		TextView d = (TextView) findViewById(R.id.wikiAuthorTextView);
		d.setText(wiki.getAuthor());
	}

	private void setSource() {
		TextView d = (TextView) findViewById(R.id.wikiSourceTextView);
		d.setText(wiki.getSource());
	}

	private void setGenDate() {
		TextView d = (TextView) findViewById(R.id.wikiGenDateTextView);
		d.setText(wiki.getGendateAsString());
	}

	private void setIcon() {
		ImageView iconview = (ImageView) findViewById(R.id.wikiIcon);
		if (wiki.hasIcon()){
			iconview.setImageBitmap(BitmapFactory.decodeStream(wiki.getIcon()));
		} else {
			AssetManager am = getAssets();
			try {
				InputStream in = am.open("icons/wiki-default-icon.png");
				iconview.setImageBitmap(BitmapFactory.decodeStream(in));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void setSelected() {
		iconview.setChecked(wiki.isSelected());
		iconview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String key = getString(R.string.config_key_selecteddbfiles);
				ArrayList<String> namelist = wiki.getDBFilesnamesAsList();
				config.edit().putString(key ,TextUtils.join(",", namelist)).commit();
				String key2 = getString(R.string.config_key_should_update_db);
				config.edit().putBoolean(key2, true).commit();
				iconview.setChecked(true);
			}
		});
	}
}
