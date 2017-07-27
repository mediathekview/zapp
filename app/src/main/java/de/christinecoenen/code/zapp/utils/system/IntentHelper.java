package de.christinecoenen.code.zapp.utils.system;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import de.christinecoenen.code.zapp.R;

public class IntentHelper {

	/**
	 * Open the given url in a new (external) activity. If no app is found
	 * that can handle this intent, a Toast is shown.
	 * @param context
	 * @param url
	 */
	public static void openUrl(Context context, String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		try {
			context.startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.error_no_app_for_link, Toast.LENGTH_LONG).show();
		}
	}

}
