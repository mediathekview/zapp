package de.christinecoenen.code.zapp.app.livestream.repository

import de.christinecoenen.code.zapp.app.livestream.api.model.Channel
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import timber.log.Timber
import java.util.*

/**
 * Cache for currently running show on any channel.
 */
internal class Cache {

	private val shows: MutableMap<Channel, LiveShow> = EnumMap(Channel::class.java)

	fun save(channel: Channel, show: LiveShow) {
		shows[channel] = show
	}

	/**
	 * @return currently running show or null in case of cache miss
	 */
	fun getShow(channel: Channel): LiveShow? {
		val show = shows[channel]

		return when {
			show == null -> {
				Timber.d("cache miss: %s", channel)
				null
			}

			show.endTime == null -> {
				Timber.d("cache miss: %s, no show end time", channel)
				null
			}

			show.endTime!!.isBeforeNow -> {
				Timber.d("cache miss: %s, show too old", channel)
				shows.remove(channel)
				null
			}

			else -> {
				Timber.d("cache hit: %s - %s", channel, show.title)
				show
			}
		}
	}
}
