package de.christinecoenen.code.programguide.plugins.zdf;


import android.util.Log;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.christinecoenen.code.programguide.model.Show;

class ZdfParser {

	private static final String TAG = ZdfParser.class.getSimpleName();

	// 2017-03-11T17:27:00+01:00
	private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

	static Show parse(JSONObject json) throws JSONException {
		Show show = new Show();

		JSONArray broadcasts = json.getJSONArray("http://zdf.de/rels/cmdm/broadcasts");
		JSONObject broadcast = broadcasts.getJSONObject(0);

		String title = broadcast.getString("title");
		String subtitle = getOptionalString(broadcast, "subtitle");
		String description = getOptionalString(broadcast, "text");
		String startTime = broadcast.getString("airtimeBegin");
		String endTime = broadcast.getString("airtimeEnd");

		show.setTitle(title);
		show.setSubtitle(subtitle);
		show.setDescription(description);
		show.setStartTime(formatter.parseDateTime(startTime));
		show.setEndTime(formatter.parseDateTime(endTime));

		return show;
	}

	private static String getOptionalString(JSONObject json, String key) {
		try {
			String value = json.getString(key);
			return value.equals("null") ? null : value;
		} catch (JSONException e) {
			Log.d(TAG, "optional json key is not present: " + key);
		}
		return null;
	}
}
