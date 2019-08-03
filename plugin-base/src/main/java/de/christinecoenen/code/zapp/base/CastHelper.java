package de.christinecoenen.code.zapp.base;

import android.content.Context;
import android.content.Intent;

public class CastHelper {

	public static void cast(Context context, String url) {
		Intent intent = new Intent(Constants.ACTION_CAST);
		intent.putExtra(Constants.EXTRA_CAST_URL, url);
		intent = Intent.createChooser(intent, "Test"); // TODO: add correct text
		context.startActivity(intent);
	}

}
