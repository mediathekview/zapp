package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.christinecoenen.code.zapp.utils.system.IntentHelper;


public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
			IntentHelper.openDownloadManager(context);
		}
	}
}
