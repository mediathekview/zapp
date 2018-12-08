package de.christinecoenen.code.zapp.utils.system;


import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import de.christinecoenen.code.zapp.R;

public class IntentHelper {

	/**
	 * Open the given url in a new (external) activity. If no app is found
	 * that can handle this intent, a system chooser is shown.
	 *
	 * @param context
	 * @param url
	 */
	public static void openUrl(Context context, String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		try {
			context.startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {
			context.startActivity(Intent.createChooser(browserIntent, context.getString(R.string.action_open)));
		}
	}

	/**
	 * Open the download manager app. If no app is found that can handle
	 * this intent, a a system chooser is shown.
	 *
	 * @param context
	 */
	public static void openDownloadManager(Context context) {
		Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		downloadManagerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			context.startActivity(downloadManagerIntent);
		} catch (ActivityNotFoundException e) {
			context.startActivity(Intent.createChooser(downloadManagerIntent, context.getString(R.string.action_open)));
		}
	}
}
