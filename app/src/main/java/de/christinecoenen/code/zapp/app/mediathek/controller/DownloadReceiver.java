package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.christinecoenen.code.zapp.utils.system.IntentHelper;

import static com.tonyodev.fetch2.FetchIntent.EXTRA_ACTION_TYPE;


public class DownloadReceiver extends BroadcastReceiver {

	public static final int ACTION_NOTIFICATION_CLICKED = 42;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null) {
			return;
		}

		int actionType = intent.getIntExtra(EXTRA_ACTION_TYPE, ACTION_NOTIFICATION_CLICKED);

		if (actionType == ACTION_NOTIFICATION_CLICKED) {
			IntentHelper.openDownloadManager(context);
		}
	}
}
