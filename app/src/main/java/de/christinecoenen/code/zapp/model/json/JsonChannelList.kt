package de.christinecoenen.code.zapp.model.json


import android.content.Context
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.model.ChannelModel
import de.christinecoenen.code.zapp.model.IChannelList
import org.apache.commons.io.IOUtils

/**
 * Loads channel data from a json file bundled with
 * the app. This operation is blocking. For the number
 * of channels provided by the app this approach works
 * just fine.
 */
class JsonChannelList(private val context: Context) : IChannelList {

	private val channels: List<ChannelModel>

	/**
	 * @return content of R.raw.channels json file
	 */
	private val jsonString: String
		get() = context.resources.openRawResource(R.raw.channels).use { inputStream ->
			return IOUtils.toString(inputStream, "UTF-8")
		}

	init {
		val parser = JsonChannelsParser(context)
		channels = parser.parse(jsonString)
	}

	override val list: List<ChannelModel>
		get() = channels

	override fun get(index: Int): ChannelModel {
		return channels[index]
	}

	override fun get(id: String): ChannelModel {
		return channels.find { channelModel -> channelModel.id == id }!!
	}

	override fun size(): Int {
		return channels.size
	}

	override fun iterator(): Iterator<ChannelModel> {
		return channels.iterator()
	}

	override fun indexOf(channelId: String): Int {
		return channels.withIndex().find { indexedValue ->
			indexedValue.value.id == channelId
		}!!.index
	}
}
