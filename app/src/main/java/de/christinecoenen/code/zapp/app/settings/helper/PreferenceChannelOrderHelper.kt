package de.christinecoenen.code.zapp.app.settings.helper

import android.content.Context
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import kotlin.math.max

/**
 * Helps persisting and reloading the channel order.
 * SharedPreferences are used to store the values on disk.
 */
class PreferenceChannelOrderHelper(context: Context) {

	companion object {
		private const val PREF_KEY_CHANNEL_ORDER = "PREF_KEY_CHANNEL_ORDER"
		private const val PREF_KEY_CHANNELS_NOT_VISIBLE = "PREF_KEY_CHANNELS_NOT_VISIBLE"
	}

	private val preferenceHelper = PreferenceHelper(context)

	fun saveChannelOrder(channels: List<ChannelModel>) {
		saveOrder(channels)
		saveVisibility(channels)
	}

	fun sortChannelList(channels: List<ChannelModel>, removeDisabled: Boolean): List<ChannelModel> {
		val sortedChannels = loadOrder(channels)
		return loadVisibility(sortedChannels, removeDisabled)
	}

	private fun loadOrder(channels: List<ChannelModel>): List<ChannelModel> {
		val sortedChannelIds = preferenceHelper.loadList(PREF_KEY_CHANNEL_ORDER)
			?: return channels // have never been saved before

		val size = max(sortedChannelIds.size, channels.size)
		var unsavedIndex = sortedChannelIds.size
		val sortedChannelArray = arrayOfNulls<ChannelModel>(size)

		for (channel in channels) {
			var index = sortedChannelIds.indexOf(channel.id)

			if (index == -1) {
				// order for this channel has never been saved - move to end
				index = unsavedIndex++
			}

			sortedChannelArray[index] = channel
		}

		// return without null values in case channels have been deleted
		return sortedChannelArray.filterNotNull()
	}

	private fun loadVisibility(channels: List<ChannelModel>, removeDisabled: Boolean): List<ChannelModel> {
		val disabledChannelIds = preferenceHelper.loadList(PREF_KEY_CHANNELS_NOT_VISIBLE)
			?: return channels // have never been saved before

		if (removeDisabled) {
			return channels.filterNot {
				disabledChannelIds.contains(it.id)
			}
		}

		for (channel in channels) {
			val isDisabled = disabledChannelIds.contains(channel.id)
			channel.isEnabled = !isDisabled
		}

		return channels
	}

	private fun saveOrder(channels: List<ChannelModel>) {
		val sortedChannelIds = channels.map { it.id }
		preferenceHelper.saveList(PREF_KEY_CHANNEL_ORDER, sortedChannelIds)
	}

	private fun saveVisibility(channels: List<ChannelModel>) {
		val disabledChannelIds = channels
			.filterNot { it.isEnabled }
			.map { it.id }

		preferenceHelper.saveList(PREF_KEY_CHANNELS_NOT_VISIBLE, disabledChannelIds)
	}
}
