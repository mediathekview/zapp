package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.christinecoenen.code.zapp.app.MainActivity;

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
			// bring running zapp instance to front
			Intent zappIntent = new Intent(context, MainActivity.class);
			zappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			zappIntent.setAction(Intent.ACTION_MAIN);
			zappIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			context.startActivity(zappIntent);
		}
	}
}
