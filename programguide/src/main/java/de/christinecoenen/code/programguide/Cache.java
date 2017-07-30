package de.christinecoenen.code.programguide;


import java.util.HashMap;
import java.util.Map;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import timber.log.Timber;

/**
 * Cache singleton for currently running show on any channel.
 */
public class Cache {

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
	 * @return currently running show or null in case of cache miss
	 */
	public Show getShow(Channel channel) {
		Show show = shows.get(channel);

		if (show == null) {
			Timber.d("cache miss: " + channel);
			return null;
		}

		if (show.getEndTime() == null) {
			Timber.d("cache miss: " + channel + ", no show end time");
			return null;
		}

		if (show.getEndTime().isBeforeNow()) {
			Timber.d("cache miss: " + channel + ", show too old");
			shows.remove(channel);
			return null;
		}

		Timber.d("cache hit: " + channel + " - " + show);

		return show;
	}
}
