package de.christinecoenen.code.zapp.models.channels

/**
 * Simple IChannelList wrapper around a List of ChannelModels.
 */
class SimpleChannelList(private var channels: List<ChannelModel>) : IChannelList {

	override val list
		get() = channels

	override fun get(index: Int) = channels[index]

	override fun get(id: String): ChannelModel? {
		val index = indexOf(id)
		return if (index == -1) null else get(index)
	}

	override fun replaceAllChannels(channels: List<ChannelModel>) {
		this.channels = channels
	}

	override fun size() = channels.size

	override fun indexOf(channelId: String) = channels.indexOfFirst { it.id == channelId }

	override fun iterator() = channels.listIterator()
}
