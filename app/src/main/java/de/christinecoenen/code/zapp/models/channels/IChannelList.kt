package de.christinecoenen.code.zapp.models.channels

interface IChannelList : Iterable<ChannelModel> {

	operator fun get(index: Int): ChannelModel

	operator fun get(id: String): ChannelModel?

	fun size(): Int

	val list: List<ChannelModel>

	fun indexOf(channelId: String): Int
}
