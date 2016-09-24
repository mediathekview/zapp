package de.christinecoenen.code.programguide.plugins.arte;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.christinecoenen.code.programguide.model.Show;

/**
 * Parses ARTE program info json.
 * @see "https://api.arte.tv/api/player/v1/livestream/de"
 */
class ArteParser {

	private static final String TAG = ArteParser.class.getSimpleName();

	static Show parse(JSONObject json) throws JSONException {
		Show show = new Show();

		JSONObject root = json.getJSONObject("videoJsonPlayer");
		String title = root.getString("VTI");
		String subtitle = getOptionalString(root, "subtitle");
		String description = getOptionalString(root, "V7T");

		show.setTitle(title);
		show.setSubtitle(subtitle);
		show.setDescription(description);

		return show;
	}

	private static String getOptionalString(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			Log.d(TAG, "optional json key is not present: " + key);
		}
		return null;
	}
}
