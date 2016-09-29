package de.christinecoenen.code.programguide.plugins.deutschewelle;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.christinecoenen.code.programguide.model.Show;

class DeutscheWelleParser {

	@SuppressWarnings("unused")
	private static final String TAG = DeutscheWelleParser.class.getSimpleName();

	// 2016-09-29T07:15:00.000Z
	private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

	static Show parse(JSONObject json) throws JSONException {
		JSONArray showsJson = json.getJSONArray("items");

		for (int i = 0; i < showsJson.length(); i++) {
			JSONObject showJson = showsJson.getJSONObject(i);
			DateTime start = formatter.parseDateTime(showJson.getString("startDate"));
			DateTime end = formatter.parseDateTime(showJson.getString("endDate"));

			if (start.isBeforeNow() && end.isAfterNow()) {
				return parseShow(showJson, start, end);
			}
		}

		// may happen in the time between shows
		return Show.getIntermission();
	}

	private static Show parseShow(JSONObject showJson, DateTime start, DateTime end) throws JSONException {
		String title = showJson.getString("name");
		String subtitle = showJson.getString("description");
		String description = showJson.getString("programDescription");

		Show show = new Show();
		show.setTitle(title);
		show.setSubtitle(subtitle);
		show.setDescription(description);
		show.setStartTime(start);
		show.setEndTime(end);

		return show;
	}
}
