package fr.renzo.wikipoff;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastReceiverDownloadManager extends BroadcastReceiver {

	private static final String TAG = "BroadcastReceiverDownloadManager";

	public BroadcastReceiverDownloadManager() {
		super();	
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

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
}
