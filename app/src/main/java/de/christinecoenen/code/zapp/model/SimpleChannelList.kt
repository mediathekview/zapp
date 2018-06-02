package de.christinecoenen.code.zapp.model

/**
 * Simple IChannelList wrapper around a List of ChannelModels.
 */
class SimpleChannelList(private val channels: List<ChannelModel>) : IChannelList {

	override val list: List<ChannelModel>
		get() = channels

	override fun get(index: Int): ChannelModel {
		return channels[index]
	}

	override fun get(id: String): ChannelModel {
		val index = indexOf(id)
		return this[index]
	}

	override fun size(): Int {
		return channels.size
	}

	override fun indexOf(channelId: String): Int {
		return channels.withIndex().find { indexedValue ->
			indexedValue.value.id == channelId
		}!!.index
	}

	override fun iterator(): Iterator<ChannelModel> {
		return channels.iterator()
	}
}
