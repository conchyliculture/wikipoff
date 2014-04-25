package fr.renzo.wikipoff;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class BroadcastReceiverDownloadManager extends BroadcastReceiver {

	private static final String TAG = "BroadcastReceiverDownloadManager";

	public BroadcastReceiverDownloadManager() {
		super();	
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		checkDownloadStatus(context);
		if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
			Intent outputintent = new Intent(context.getApplicationContext(), StopDownloadActivity.class);
//			Bundle bundle = intent.getExtras();
//
//			long downloadId = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -12);
//			if (downloadId == -12) {
//				// we're desperate
//				long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS ); 
//				
//				outputintent.putExtra("dlids", ids);
//			} else {
//				outputintent.putExtra("dlid", downloadId);
//			}
			
			outputintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(outputintent);
		}
	}
	    private void checkDownloadStatus(Context context){
			DownloadManager.Query query = new DownloadManager.Query();
			
			DownloadManager downloadManager =(DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			Cursor cursor = downloadManager.query(query);
			if(cursor.moveToFirst()){
				int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = cursor.getInt(columnIndex);
				int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
				int reason = cursor.getInt(columnReason);
				
				statusMessage(context, status, reason);
			}
			cursor.close();

	    }
	private void statusMessage(Context context, int status,int reason){
		switch(status){

		case DownloadManager.STATUS_FAILED:
			String failedReason = "unknown reason: "+reason;
			switch(reason){
			case 404:
				failedReason = "Download returned 404 error";
				break;
			case DownloadManager.ERROR_CANNOT_RESUME:
				failedReason = "ERROR_CANNOT_RESUME";
				break;
			case DownloadManager.ERROR_DEVICE_NOT_FOUND:
				failedReason = "ERROR_DEVICE_NOT_FOUND";
				break;
			case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
				failedReason = "ERROR_FILE_ALREADY_EXISTS";
				break;
			case DownloadManager.ERROR_FILE_ERROR:
				failedReason = "ERROR_FILE_ERROR";
				break;
			case DownloadManager.ERROR_HTTP_DATA_ERROR:
				failedReason = "ERROR_HTTP_DATA_ERROR";
				break;
			case DownloadManager.ERROR_INSUFFICIENT_SPACE:
				failedReason = "ERROR_INSUFFICIENT_SPACE";
				break;
			case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
				failedReason = "ERROR_TOO_MANY_REDIRECTS";
				break;
			case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
				failedReason = "ERROR_UNHANDLED_HTTP_CODE";
				break;
			case DownloadManager.ERROR_UNKNOWN:
				failedReason = "ERROR_UNKNOWN";
				break;
			}
			Log.d(TAG,"FAILED: " + failedReason);
			Toast.makeText(context,
					"FAILED: " + failedReason,
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_PAUSED:
			String pausedReason = "";

			switch(reason){
			case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
				pausedReason = "PAUSED_QUEUED_FOR_WIFI";
				break;
			case DownloadManager.PAUSED_UNKNOWN:
				pausedReason = "PAUSED_UNKNOWN";
				break;
			case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
				pausedReason = "PAUSED_WAITING_FOR_NETWORK";
				break;
			case DownloadManager.PAUSED_WAITING_TO_RETRY:
				pausedReason = "PAUSED_WAITING_TO_RETRY";
				break;
			}
			Log.d(TAG,"PAUSED: " + pausedReason);
			Toast.makeText(context,
					"PAUSED: " + pausedReason,
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_PENDING:
			//		Toast.makeText(this,
			//				"PENDING",
			//				Toast.LENGTH_LONG).show();
			Log.d(TAG,"Download started");
			Toast.makeText(context,
					"Download started",
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_RUNNING:
			Log.d(TAG,"Download running");
			Toast.makeText(context,
					"RUNNING",
					Toast.LENGTH_LONG).show();
			break;
		case DownloadManager.STATUS_SUCCESSFUL:
			Log.d(TAG,"Download done");
			Toast.makeText(context,
					"SUCCESSFUL",
					Toast.LENGTH_LONG).show();
			break;
		default:
			Log.d(TAG,"WTF "+status);
			Toast.makeText(context,
					"WTF"+status,
					Toast.LENGTH_LONG).show();
			break;
		}
	}
}
