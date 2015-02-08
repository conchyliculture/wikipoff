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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.WikiDBFile;
import fr.renzo.wikipoff.WikipOff;
import fr.renzo.wikipoff.WikiDownloadService;

public class WikiAvailableActivity extends Activity{
	@SuppressWarnings("unused")
	private static final String TAG = "WikiInstalledActivity";
	public static final String DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS";
	private Wiki wiki;

	private WikipOff app;
	private String storage;
	private ArrayList<String> urls_to_dl = new ArrayList<String>() ;
	private ProgressBar pb;
	private ProgressReceiver progressReceiver;
	private TextView msg;
	private Button downloadbutton;
	private Button stopdownloadbutton;
	public boolean installed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_available_wiki);
		this.app =(WikipOff) getApplication();
		Intent intent = getIntent();
		this.storage = intent.getStringExtra("storage");
		this.wiki = (Wiki) intent.getExtras().getSerializable("wiki");
		this.installed = intent.getBooleanExtra("installed", false);
		Log.d(TAG,"is installed "+installed);
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


	@Override
	protected void onStop()
	{
		unregisterReceiver(progressReceiver);
		super.onStop();
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
		setStopDowload();
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
		downloadbutton.setOnClickListener(new downloadOnClickListener());
	}

	private void setStopDowload() {
		stopdownloadbutton = (Button) findViewById(R.id.wikiStopDownloadButton);
		stopdownloadbutton.setOnClickListener(new stopDownloadOnClickListener());
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



	private void download() {
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
		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				urls_to_dl.clear();
			}
		})
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
		pb.setVisibility(View.VISIBLE);
		downloadbutton.setVisibility(View.GONE);
		stopdownloadbutton.setVisibility(View.VISIBLE);
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
	private void stop_download() {
		pb.setVisibility(View.INVISIBLE);
		downloadbutton.setVisibility(View.VISIBLE);
		stopdownloadbutton.setVisibility(View.GONE);
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

		private long last_progress;
		private long last_update=System.nanoTime();
		private long last_speed=-1;
		@Override
		public void onReceive(Context context, Intent intent) {

			String whatsup = intent.getStringExtra("whatsup");
			String link = intent.getStringExtra("url");
			if (whatsup.equals(DOWNLOAD_PROGRESS)) {
				long progress =intent.getLongExtra("download_progress",-1);
				long size =intent.getLongExtra("download_size",-1);
				int percentage = (int) ((100 * progress)/size);

				long now = System.nanoTime();
				if (((now-last_update)/1000000000.0 >= 1)) {
					long speed = 1000000000 * (progress - last_progress)/(now-last_update);
					last_update=now;
					last_progress=progress;

					pb.setProgress(percentage);
					msg.setVisibility(View.VISIBLE);
					stopdownloadbutton.setVisibility(View.VISIBLE);
					downloadbutton.setVisibility(View.GONE);
					String txtmsg = String.format("%s %s/%s (%s/s) = %d%% (%s : %s/%d)",
							getString(R.string.downloading),
							getSizeReadable(progress),
							getSizeReadable(size),
							getSizeReadable(speed),
							percentage,
							getString(R.string.downloading_eta),
							timeLeft((size-progress)/average(speed)),
							(long)(size-progress)/average(speed)
							);
					msg.setText(txtmsg);
				} 

			} else if (whatsup.equals(DOWNLOAD_FINISHED)) {
				msg.setVisibility(View.VISIBLE);
				if (urls_to_dl.indexOf(link)+1==urls_to_dl.size()) {
					pb.setVisibility(View.INVISIBLE);
					msg.setText(getString(R.string.download_finished));
					downloadbutton.setVisibility(View.VISIBLE);
					stopdownloadbutton.setVisibility(View.GONE);
				}
			} else if (whatsup.equals(DOWNLOAD_FAILED)) {
				pb.setVisibility(View.INVISIBLE);
				String error =intent.getStringExtra("error");
				msg.setText(getString(R.string.download_failed)+" "+error);
				msg.setVisibility(View.VISIBLE);
				downloadbutton.setVisibility(View.VISIBLE);
				stopdownloadbutton.setVisibility(View.GONE);
				urls_to_dl.remove(link);
			}
		}
		private String getSizeReadable(long size) {
			return getSizeReadable(size, true);
		}

		private String getSizeReadable(long size, boolean si) {

			int unit = si ? 1000 : 1024;
			if (size < unit) {
				return Long.toString(size) + " B";
			}
			int exp = (int) (Math.log(size) / Math.log(unit));
			String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
			return String.format("%.1f %sB", size / Math.pow(unit, exp), pre);
		}

		private long average(long value) {
			if (last_speed == -1) {
				last_speed = value;
				return value;
			}
			double newValue = last_speed + 0.7 * (value - last_speed);
			last_speed = (long) newValue;
			return (long) newValue;
		}
		private String timeLeft(long seconds) {
			int h = (int) Math.floor((seconds %= 86400) / 3600);
		    int m = (int) Math.floor((seconds %= 3600) / 60);
		    int s = (int) (seconds % 60);
		    return String.format("%s:%s:%s", h,m,s);
		}
	};

	public class downloadOnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {

			if (!installed) {
				// We can't file the files, so it's not there, we can d/l
				download();

			} else {
				// There are files already, is it an older copy of the wiki?
				//			if (context.olderPresent(wiki)) {
				// IT is ! Let's be sure the user wants to overwrite it
				new AlertDialog.Builder(WikiAvailableActivity.this)
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
			}
		}
	};
	
	public class stopDownloadOnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {

			new AlertDialog.Builder(WikiAvailableActivity.this)
			.setTitle(getString(R.string.message_warning))
			.setMessage(getString(R.string.message_stop_download))
			.setNegativeButton(getString(R.string.no), null )
			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					stop_download();
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
		}

	};
}
