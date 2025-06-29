package de.christinecoenen.code.zapp.models.channels.json

import android.content.Context
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.IChannelList
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import java.io.IOException

/**
 * Loads channel data from a json file bundled with
 * the app. This operation is blocking. For the number
 * of channels provided by the app this approach works
 * just fine.
 */
class JsonChannelList(private val context: Context) : IChannelList {

	override var list: List<ChannelModel>

	init {
		val parser = JsonChannelsParser(context)
		list = parser.parse(getJsonString())
	}

	override fun get(index: Int) = list[index]

	override fun get(id: String): ChannelModel? {
		val index = indexOf(id)
		return if (index == -1) null else get(index)
	}

	override fun replaceAllChannels(channels: List<ChannelModel>) {
		list = channels
	}

	override fun size() = list.size

	override fun iterator() = list.iterator()

	override fun indexOf(channelId: String) = list.indexOfFirst { it.id == channelId }

	/**
	 * @return content of R.raw.channels json file
	 */
	private fun getJsonString(): String {
		try {
			return context.resources.readAllText(R.raw.channels)
		} catch (e: IOException) {
			// we know, this file is bundled with the app,
			// so this should never happen
			throw RuntimeException(e)
		}
	}
}
