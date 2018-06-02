package de.christinecoenen.code.zapp.model.json


import android.content.Context
import de.christinecoenen.code.zapp.app.settings.helper.PreferenceChannelOrderHelper
import de.christinecoenen.code.zapp.model.ChannelModel
import de.christinecoenen.code.zapp.model.IChannelList
import de.christinecoenen.code.zapp.model.ISortableChannelList
import de.christinecoenen.code.zapp.model.SimpleChannelList

class SortableJsonChannelList(context: Context) : ISortableChannelList {

	private var channelList: IChannelList = JsonChannelList(context)
	private val channelOrderHelper: PreferenceChannelOrderHelper = PreferenceChannelOrderHelper(context)

	override val list: List<ChannelModel>
		get() = channelList.list

	init {
		loadSortingFromDisk()
	}

	override fun get(index: Int): ChannelModel {
		return channelList[index]
	}

	override fun get(id: String): ChannelModel {
		return channelList[id]
	}

	override fun size(): Int {
		return channelList.size()
	}

	override fun iterator(): Iterator<ChannelModel> {
		return channelList.iterator()
	}

	override fun indexOf(channelId: String): Int {
		return channelList.indexOf(channelId)
	}

	override fun reloadChannelOrder() {
		loadSortingFromDisk()
	}

	override fun persistChannelOrder() {
		channelOrderHelper.saveChannelOrder(channelList.list)
	}

	private fun loadSortingFromDisk() {
		val sortedChannels = channelOrderHelper.sortChannelList(channelList.list)
		channelList = SimpleChannelList(sortedChannels)
	}
}
