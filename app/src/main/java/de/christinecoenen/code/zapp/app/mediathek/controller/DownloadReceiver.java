package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
			Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
			downloadManagerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(downloadManagerIntent);
		}
	}
}
