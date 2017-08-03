package de.christinecoenen.code.zapp.app.livestream.api;


import java.util.HashMap;
import java.util.Map;

import de.christinecoenen.code.zapp.app.livestream.api.model.Channel;
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow;
import timber.log.Timber;

/**
 * Cache singleton for currently running show on any channel.
 */
class Cache {

	private static Cache instance = null;

	private final Map<Channel, LiveShow> shows = new HashMap<>();

	static Cache getInstance() {
		if (instance == null) {
			instance = new Cache();
		}
		return instance;
	}

	public void save(Channel channel, LiveShow show) {
		this.shows.put(channel, show);
	}

	/**
	 * @return currently running show or null in case of cache miss
	 */
	LiveShow getShow(Channel channel) {
		LiveShow show = shows.get(channel);

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
