package fr.renzo.wikipoff;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastReceiverDownloadManager extends BroadcastReceiver {

	public BroadcastReceiverDownloadManager() {
		super();	
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
			Intent i = new Intent(context.getApplicationContext(), StopDownloadActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}
