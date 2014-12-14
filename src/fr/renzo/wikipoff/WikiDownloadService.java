package fr.renzo.wikipoff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import fr.renzo.wikipoff.ui.activities.WikiAvailableActivity.ProgressReceiver;

public class WikiDownloadService extends IntentService {
	public final class Constants {
	    // Defines a custom Intent action
	    public static final String BROADCAST_ACTION =
	        "com.example.android.threadsample.BROADCAST";
	    // Defines the key for the status "extra" in an Intent
	    public static final String EXTENDED_DATA_STATUS =
	        "com.example.android.threadsample.STATUS";
	}

	private static final String TAG = "WikiDownloadService";
	public WikiDownloadService() {
		super("WikiDownloadService");
	}
	public WikiDownloadService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent workIntent) {
		URL url;
		try {
			url = new URL(workIntent.getStringExtra("url"));
		String outputdir = workIntent.getStringExtra("outputdir");
		String filename = workIntent.getStringExtra("filename");
		long size = workIntent.getLongExtra("size",-1);
		
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.connect();
		InputStream input = new BufferedInputStream(con.getInputStream());

		File file = new File(outputdir,filename);

		OutputStream output = new FileOutputStream(file);
		byte data[] = new byte[8192];
		long total = 0;
		int count;
		while ((count = input.read(data)) != -1) {
			total += count;
			
			publishProgress((int) (total * 100 / size));
			output.write(data, 0, count);
			
		}
		output.flush();
		output.close();
		input.close();
		publishFinished();
		} catch (SocketTimeoutException e) {
			Log.d(TAG,"Timeout...");
			publishFailed("Timeout error");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Log.d(TAG,"MalformedURLException");
			publishFailed("MalformedURLException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG,"IOException"+e.getMessage());
			publishFailed("IOException"+e.getMessage());
			e.printStackTrace();
		}
	}
	private void publishFailed(String error) {
		/*create new intent to broadcast our processed data to our activity*/
		Intent resultBroadCastIntent =new Intent();
		/*set action here*/
		resultBroadCastIntent.setAction(ProgressReceiver.ACTION_DOWNLOAD_INFO);
		/*set intent category as default*/
		resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
 
		/*add data to intent*/
		resultBroadCastIntent.putExtra("whatsup", ProgressReceiver.DOWNLOAD_FAILED);
		resultBroadCastIntent.putExtra("error", error);
		/*send broadcast */
		sendBroadcast(resultBroadCastIntent);
	}
	
	private void publishFinished() {
		/*create new intent to broadcast our processed data to our activity*/
		Intent resultBroadCastIntent =new Intent();
		/*set action here*/
		resultBroadCastIntent.setAction(ProgressReceiver.ACTION_DOWNLOAD_INFO);
		/*set intent category as default*/
		resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
 
		/*add data to intent*/
		resultBroadCastIntent.putExtra("whatsup", ProgressReceiver.DOWNLOAD_FINISHED);
		/*send broadcast */
		sendBroadcast(resultBroadCastIntent);
	}

	private void publishProgress(int i) {
		/*create new intent to broadcast our processed data to our activity*/
		Intent resultBroadCastIntent =new Intent();
		/*set action here*/
		resultBroadCastIntent.setAction(ProgressReceiver.ACTION_DOWNLOAD_INFO);
		/*set intent category as default*/
		resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
 
		/*add data to intent*/
		resultBroadCastIntent.putExtra("whatsup", ProgressReceiver.DOWNLOAD_PROGRESS);
		resultBroadCastIntent.putExtra("download_progress", i);
		/*send broadcast */
		sendBroadcast(resultBroadCastIntent);
	}

}
