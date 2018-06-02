package de.christinecoenen.code.zapp.model

interface IChannelList : Iterable<ChannelModel> {

	val list: List<ChannelModel>

	operator fun get(index: Int): ChannelModel
	operator fun get(id: String): ChannelModel
	fun size(): Int
	fun indexOf(channelId: String): Int

}
