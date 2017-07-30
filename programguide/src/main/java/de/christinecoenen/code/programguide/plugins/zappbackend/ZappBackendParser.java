package de.christinecoenen.code.programguide.plugins.zappbackend;


import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.christinecoenen.code.programguide.model.Show;
import timber.log.Timber;

class ZappBackendParser {

	// 2017-03-11T17:27:00+01:00
	private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

	static Show parse(JSONObject json) throws JSONException {
		Show show = new Show();

		JSONArray shows = json.getJSONArray("shows");
		JSONObject showJson = shows.getJSONObject(0);

		String title = showJson.getString("title");
		String subtitle = getOptionalString(showJson, "subtitle");
		String startTime = getOptionalString(showJson, "startTime");
		String endTime = getOptionalString(showJson, "endTime");

		show.setTitle(title);
		show.setSubtitle(subtitle);
		if (startTime != null && endTime != null) {
			show.setStartTime(formatter.parseDateTime(startTime));
			show.setEndTime(formatter.parseDateTime(endTime));
		}

		return show;
	}

	private static String getOptionalString(JSONObject json, String key) {
		try {
			String value = json.getString(key);
			return value.equals("null") ? null : value;
		} catch (JSONException e) {
			Timber.d("optional json key is not present: " + key);
		}
		return null;
	}
}
