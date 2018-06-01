package de.christinecoenen.code.zapp.app.livestream.api


import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import timber.log.Timber
import java.util.*

/**
 * Cache singleton for currently running show on any channel.
 */
internal class Cache private constructor() {

	private val shows = HashMap<Channel, LiveShow>()

	fun save(channel: Channel, show: LiveShow) {
		this.shows[channel] = show
	}

	/**
	 * @return currently running show or null in case of cache miss
	 */
	fun getShow(channel: Channel): LiveShow? {
		val show = shows[channel]

		if (show == null) {
			Timber.d("cache miss: %s", channel)
			return null
		}

		if (show.endTime == null) {
			Timber.d("cache miss: %s, no show end time", channel)
			return null
		}

		if (show.endTime!!.isBeforeNow) {
			Timber.d("cache miss: %s, show too old", channel)
			shows.remove(channel)
			return null
		}

		Timber.d("cache hit: %s - %s", channel, show)

		return show
	}

	companion object {

		private val instance: Cache = Cache()

		@JvmStatic
		@Synchronized
		fun getInstance(): Cache {
			return instance
		}
	}
}
