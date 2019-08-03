package de.christinecoenen.code.zapp.app.livestream.repository;


import java.util.HashMap;
import java.util.Map;

import de.christinecoenen.code.zapp.app.livestream.api.model.Channel;
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow;
import timber.log.Timber;

/**
 * Cache for currently running show on any channel.
 */
class Cache {

	private final Map<Channel, LiveShow> shows = new HashMap<>();

	public void save(Channel channel, LiveShow show) {
		this.shows.put(channel, show);
	}

	/**
	 * @return currently running show or null in case of cache miss
	 */
	LiveShow getShow(Channel channel) {
		LiveShow show = shows.get(channel);

		if (show == null) {
			Timber.d("cache miss: %s", channel);
			return null;
		}

		if (show.getEndTime() == null) {
			Timber.d("cache miss: %s, no show end time", channel);
			return null;
		}

		if (show.getEndTime().isBeforeNow()) {
			Timber.d("cache miss: %s, show too old", channel);
			shows.remove(channel);
			return null;
		}

		Timber.d("cache hit: %s - %s", channel, show);

		return show;
	}
}
