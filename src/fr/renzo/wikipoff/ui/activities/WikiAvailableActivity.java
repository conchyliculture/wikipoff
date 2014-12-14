package fr.renzo.wikipoff.ui.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.WikiDBFile;
import fr.renzo.wikipoff.WikiDownloadService;
import fr.renzo.wikipoff.WikiException;

public class WikiAvailableActivity extends Activity implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "WikiInstalledActivity";
	public static final String DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS";
	private Wiki wiki;

	private String storage;
	private ArrayList<String> urls_to_dl = new ArrayList<String>() ;
	private ProgressBar pb;
	private ProgressReceiver progressReceiver;
	private TextView msg;
	private Button downloadbutton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_available_wiki);

		Intent intent = getIntent();
		this.storage = intent.getStringExtra("storage");
		this.wiki = (Wiki) intent.getExtras().getSerializable("wiki");
		// WARNING Wiki needs a context, it was lost on serializing...
		wiki.setContext(this);

		setTitle(wiki.getType()+" - "+wiki.getLangcode());

		setViews();

		/*create filter for exact intent what we want from other intent*/
		IntentFilter intentFilter =new IntentFilter(ProgressReceiver.ACTION_DOWNLOAD_INFO);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		/* create new broadcast receiver*/
		progressReceiver=new ProgressReceiver();
		/* registering our Broadcast receiver to listen action*/
		registerReceiver(progressReceiver, intentFilter);
	}

	private void setViews() {
		setIcon();
		setLanguage();
		setGenDate();
		setSource();
		setAuthor();
		setType();
		setSize();
		setFiles();
		setDowload();
		this.pb = (ProgressBar) findViewById(R.id.downloadprogress);
		this.msg = (TextView) findViewById(R.id.message);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == WikiManagerActivity.REQUEST_DELETE_CODE) {
			setResult(resultCode);
			finish();
		} 
	}

	private void setDowload() {
		downloadbutton = (Button) findViewById(R.id.wikiDownloadButton);
		downloadbutton.setOnClickListener(this);
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
		if (wiki.hasIconURL()){
			// TODO
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

	@Override
	public void onClick(View arg0) {

		boolean missing=true;
		try {
			missing = wiki.isMissing();
		} catch (WikiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (missing) {
			// We can't file the files, so it's not there, we can d/l
			download();

		} else {
			// There are files already, is it an older copy of the wiki?
			//			if (context.olderPresent(wiki)) {
			// IT is ! Let's be sure the user wants to overwrite it
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.message_warning))
			.setMessage(getString(R.string.message_validate_download_olderpresent))
			.setNegativeButton(getString(R.string.no), null )
			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					download();
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
			//			} else {
			//				Toast.makeText(this, getString(R.string.message_wiki_already_installed), Toast.LENGTH_LONG).show();
			//			}

		}

	}

	private void download() {
		//		if (context.isInCurrentDownloads(Integer.valueOf(position))) {
		//			Toast.makeText(context, getString(R.string.message_download_already_running), Toast.LENGTH_LONG).show();
		//		} else {

		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		int nb_of_files= wiki.getFilenamesAsString().split("\\+").length;
		String msg = "";

		if (wifi.isConnected()) {
			if (nb_of_files>1) {
				msg = getString(R.string.message_validate_download_n,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true),
						nb_of_files);
			} else {
				msg = getString(R.string.message_validate_download,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true));
			} 
		}else {
			if (nb_of_files>1) {
				msg = getString(R.string.message_validate_download_n_nowifi,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true),
						nb_of_files);
			} else {
				msg = getString(R.string.message_validate_download_nowifi,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true));
			} 

		}
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.message_warning))
		.setMessage(msg)
		.setNegativeButton(getString(R.string.no), null)
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				do_download();
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();

		//		}

	}
	private void do_download() {
		//View v = availablewikislistview.getChildAt(position);
		//		context.addToCurrentDownloads(Integer.valueOf(position),wiki.getFilenamesAsString());
		//		if (v!=null) {
		//			ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloadprogress);
		//			pb.setVisibility(View.VISIBLE);
		//		}
		//		this.downloadFile = new DownloadFile();
		//		downloadFile.execute(wiki,Integer.valueOf(position));
		pb.setVisibility(View.VISIBLE);
		downloadbutton.setVisibility(View.INVISIBLE);
		for(WikiDBFile wdbf : wiki.getDBFiles()) {
			this.urls_to_dl.add(wdbf.getUrl());
			Intent myIntent= new Intent(WikiAvailableActivity.this,WikiDownloadService.class);
			// add necessary  data to intent
			myIntent.putExtra("url", wdbf.getUrl());
			myIntent.putExtra("outputdir",  new File(storage,getString(R.string.DBDir)).getAbsolutePath());
			myIntent.putExtra("filename", wdbf.getFilename());
			myIntent.putExtra("size", wdbf.getSize());
			// start service
			startService(myIntent);
		}

	}


	public class ProgressReceiver extends BroadcastReceiver {
		/**
		 * action string for our broadcast receiver to get notified
		 */
		public final static String ACTION_DOWNLOAD_INFO= "fr.renzo.wikipoff.download.DOWNLOAD_INFO";

		public final static String DOWNLOAD_PROGRESS= "fr.renzo.wikipoff.download.DOWNLOAD_PROGRESS";
		public final static String DOWNLOAD_FAILED= "fr.renzo.wikipoff.download.DOWNLOAD_FAILED";
		public final static String DOWNLOAD_FINISHED= "fr.renzo.wikipoff.download.DOWNLOAD_FINISHED";
		@Override
		public void onReceive(Context context, Intent intent) {
			String whatsup = intent.getStringExtra("whatsup");
			String link = intent.getStringExtra("url");
			if (whatsup.equals(DOWNLOAD_PROGRESS)) {
				int result =intent.getIntExtra("download_progress",-1);
				pb.setProgress(result);
				msg.setVisibility(View.VISIBLE);
				msg.setText(getString(R.string.downloading)+" "+(urls_to_dl.indexOf(link)+1)+"/"+urls_to_dl.size());
			} else if (whatsup.equals(DOWNLOAD_FINISHED)) {
				msg.setVisibility(View.VISIBLE);
				if (urls_to_dl.indexOf(link)+1==urls_to_dl.size()) {
					pb.setVisibility(View.INVISIBLE);
					msg.setText(getString(R.string.download_finished));
					downloadbutton.setVisibility(View.VISIBLE);
				}
			} else if (whatsup.equals(DOWNLOAD_FAILED)) {
				pb.setVisibility(View.INVISIBLE);
				String error =intent.getStringExtra("error");
				msg.setText(getString(R.string.download_failed)+" "+error);
				msg.setVisibility(View.VISIBLE);
				downloadbutton.setVisibility(View.VISIBLE);
			}
		}
	};


}
