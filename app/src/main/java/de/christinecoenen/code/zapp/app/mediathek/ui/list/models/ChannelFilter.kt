package de.christinecoenen.code.zapp.app.mediathek.ui.list.models

import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel

data class ChannelFilter(
	private val channelMap: MutableMap<MediathekChannel, Boolean> = MediathekChannel.values()
		.associateWith { false }
		.toMutableMap()
) {
	val isApplied: Boolean
		get() = channelMap.containsValue(true)

	/**
	 * @return True if channel value has been changed by this method call.
	 */
	fun setEnabled(channel: MediathekChannel, isEnabled: Boolean): Boolean {
		val wasEnabled = channelMap[channel]
		channelMap[channel] = isEnabled
		return wasEnabled != isEnabled
	}

	operator fun iterator(): Iterator<MutableMap.MutableEntry<MediathekChannel, Boolean>>{
		return channelMap.iterator()
	}

	fun copy() = ChannelFilter(channelMap.toMutableMap())
}
