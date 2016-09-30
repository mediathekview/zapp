package de.christinecoenen.code.programguide.plugins.zdf;


import android.util.ArrayMap;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;

/**
 * Parses program info json of all ZDF related channels.
 * @see "http://sofa01.zdf.de/epgservice/v2/all/now/json"
 */
class ZdfParser {

	private static final String TAG = ZdfParser.class.getSimpleName();

	// 2016-09-22T16:10:00+02:00
	private static final DateTimeFormatter formatter =
			DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

	static Map<Channel, Show> parse(JSONObject json) throws JSONException {

		JSONObject responseJson = json.getJSONObject("response");
		JSONObject channelsJson = responseJson.getJSONObject("sender");

		Map<Channel, Show> shows = new ArrayMap<>();
		shows.put(Channel.ZDF, getChannelShow("ZDF", channelsJson));
		shows.put(Channel.KIKA, getChannelShow("KI.KA", channelsJson));
		shows.put(Channel.ZDF_INFO, getChannelShow("ZDFinfo", channelsJson));
		shows.put(Channel.ZDF_NEO, getChannelShow("ZDFneo", channelsJson));
		shows.put(Channel.PHOENIX, getChannelShow("phoenix", channelsJson));
		shows.put(Channel.DREISAT, getChannelShow("dreisat", channelsJson));

		return shows;
	}

	private static Show getChannelShow(String channelKey, JSONObject channelsJson) throws JSONException {
		JSONObject channelJson = channelsJson.getJSONObject(channelKey);
		JSONArray showsJson = channelJson.getJSONArray("sendungen");
		JSONObject showMetadataJson = ((JSONObject) showsJson.get(0)).getJSONObject("sendung");

		return parseShow(showMetadataJson);
	}

	private static Show parseShow(JSONObject showMetadata) throws JSONException {
		Show show = new Show();

		JSONObject showJson = showMetadata.getJSONObject("value");

		String title = showJson.getString("titel");
		String subtitle = getOptionalString(showJson, "untertitel");
		String description = getOptionalString(showJson, "beschreibung");
		DateTime startTime = formatter.parseDateTime(showJson.getString("time"));
		DateTime endTime = formatter.parseDateTime(showJson.getString("endTime"));

		show.setTitle(title);
		show.setSubtitle(subtitle);
		show.setDescription(description);
		show.setStartTime(startTime);
		show.setEndTime(endTime);

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
