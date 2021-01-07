package de.christinecoenen.code.zapp.models.channels.json

import android.content.Context
import de.christinecoenen.code.zapp.app.settings.helper.PreferenceChannelOrderHelper
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.IChannelList
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.SimpleChannelList

open class SortableJsonChannelList(val context: Context) : ISortableChannelList {

	val channelOrderHelper: PreferenceChannelOrderHelper = PreferenceChannelOrderHelper(context)
	var channelList: IChannelList = SimpleChannelList(listOf())

	init {
		reload()
	}

	override fun get(index: Int) = channelList[index]

	override fun get(id: String) = channelList[id]

	override fun size() = channelList.size()

	override val list = channelList.list

	override fun iterator() = channelList.iterator()

	override fun indexOf(channelId: String) = channelList.indexOf(channelId)

	final override fun reload() {
		loadSortingFromDisk()
	}

	override fun reloadChannelOrder() {
		loadSortingFromDisk()
	}

	override fun persistChannelOrder() {
		channelOrderHelper.saveChannelOrder(channelList.list)
	}

	open fun loadSortingFromDisk() {
		val listFromDisk: List<ChannelModel> = JsonChannelList(context).list
		val sortedChannels = channelOrderHelper.sortChannelList(listFromDisk, false)

		channelList = SimpleChannelList(sortedChannels)
	}
}
