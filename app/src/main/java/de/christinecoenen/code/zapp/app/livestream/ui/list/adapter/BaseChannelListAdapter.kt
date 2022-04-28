package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.IChannelList
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


abstract class BaseChannelListAdapter(
	private val channelList: IChannelList,
	private val lifecycleOwner: LifecycleOwner,
	private val listener: ListItemListener
) : RecyclerView.Adapter<ChannelViewHolder>(), KoinComponent {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val bindingAdapter = getViewBindingAdapter(layoutInflater, parent)
		return ChannelViewHolder(bindingAdapter, lifecycleOwner, listener)
	}

	override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
		val channel = channelList[position]
		val programInfoViewModel =
			ProgramInfoViewModel(holder.itemView.context.applicationContext as Application, get())
		holder.setChannel(programInfoViewModel, channel)
	}

	override fun onViewRecycled(holder: ChannelViewHolder) {
		holder.recycle()
	}

	override fun getItemCount() = channelList.size()

	override fun getItemId(position: Int) = position.toLong()

	abstract fun getViewBindingAdapter(
		layoutInflater: LayoutInflater,
		parent: ViewGroup
	): IChannelViewBindingAdapter
}
