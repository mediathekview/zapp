package de.christinecoenen.code.programguide;


import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;

/**
 * Cache singleton for currently running show on any channel.
 */
public class Cache {

	private static final String TAG = Cache.class.getSimpleName();

	private static Cache instance = null;

	private final Map<Channel, Show> shows = new HashMap<>();

	public static Cache getInstance() {
		if (instance == null) {
			instance = new Cache();
		}
		return instance;
	}

	public void save(Channel channel, Show show) {
		this.shows.put(channel, show);
	}

	public void save(Map<Channel, Show> shows) {
		this.shows.putAll(shows);
	}

	/**
	 * @return         currently running show or null in case of cache miss
     */
	public Show getShow(Channel channel) {
		Show show = shows.get(channel);

		if (show == null) {
			Log.d(TAG, "cache miss: " + channel);
			return null;
		}

		if (show.getEndTime() == null) {
			Log.d(TAG, "cache miss: " + channel + ", no show end time");
			return null;
		}

		if (show.getEndTime().isBeforeNow()) {
			Log.d(TAG, "cache miss: " + channel + ", show too old");
			shows.remove(channel);
			return null;
		}

		Log.d(TAG, "cache hit: " + channel + " - " + show);

		return show;
	}
}
