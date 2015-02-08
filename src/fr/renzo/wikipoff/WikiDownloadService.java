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

	//TODO Check if mode avion/ on a internet

	private static final String TAG = "WikiDownloadService";

	public WikiDownloadService() {
		super("WikiDownloadService");
	}
	public WikiDownloadService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent workIntent) {
		String link = workIntent.getStringExtra("url");
		URL url;
		try {
			url = new URL(link);
			String outputdir = workIntent.getStringExtra("outputdir");
			String filename = workIntent.getStringExtra("filename");
			long size = workIntent.getLongExtra("size",-1);

			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.connect();
			InputStream input = new BufferedInputStream(con.getInputStream());

			File file = new File(outputdir,filename);

			OutputStream output = new FileOutputStream(file);
			byte data[] = new byte[8192];
			long progress = 0;
			int count;
			while ((count = input.read(data)) != -1) {

				progress += count;

				publishProgress(link,progress,size);
				output.write(data, 0, count);

			}
			output.flush();
			output.close();
			input.close();
			publishFinished(link);
		} catch (SocketTimeoutException e) {
			Log.d(TAG,"Timeout...");
			publishFailed(link,"Timeout error");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Log.d(TAG,"MalformedURLException");
			publishFailed(link,"MalformedURLException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG,"IOException"+e.getMessage());
			publishFailed(link,"IOException"+e.getMessage());
			e.printStackTrace();
		}

	}
	private void publishFailed(String url,String error) {
		/*create new intent to broadcast our processed data to our activity*/
		Intent resultBroadCastIntent =new Intent();
		/*set action here*/
		resultBroadCastIntent.setAction(ProgressReceiver.ACTION_DOWNLOAD_INFO);
		/*set intent category as default*/
		resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);

		/*add data to intent*/
		resultBroadCastIntent.putExtra("whatsup", ProgressReceiver.DOWNLOAD_FAILED);
		resultBroadCastIntent.putExtra("error", error);
		resultBroadCastIntent.putExtra("url", url);
		/*send broadcast */
		sendBroadcast(resultBroadCastIntent);
	}

	private void publishFinished(String url) {
		/*create new intent to broadcast our processed data to our activity*/
		Intent resultBroadCastIntent =new Intent();
		/*set action here*/
		resultBroadCastIntent.setAction(ProgressReceiver.ACTION_DOWNLOAD_INFO);
		/*set intent category as default*/
		resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);

		/*add data to intent*/
		resultBroadCastIntent.putExtra("whatsup", ProgressReceiver.DOWNLOAD_FINISHED);
		resultBroadCastIntent.putExtra("url", url);
		/*send broadcast */
		sendBroadcast(resultBroadCastIntent);
	}

	private void publishProgress(String url,long progress, long size) {
		/*create new intent to broadcast our processed data to our activity*/
		Intent resultBroadCastIntent =new Intent();
		/*set action here*/
		resultBroadCastIntent.setAction(ProgressReceiver.ACTION_DOWNLOAD_INFO);
		/*set intent category as default*/
		resultBroadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);

		/*add data to intent*/
		resultBroadCastIntent.putExtra("whatsup", ProgressReceiver.DOWNLOAD_PROGRESS);
		resultBroadCastIntent.putExtra("download_progress", progress);
		resultBroadCastIntent.putExtra("download_size", size);
		resultBroadCastIntent.putExtra("url", url);
		/*send broadcast */
		sendBroadcast(resultBroadCastIntent);
	}

}
