package de.christinecoenen.code.zapp.app.livestream.ui.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.views.ProgramInfoViewBase
import de.christinecoenen.code.zapp.model.ChannelModel
import de.christinecoenen.code.zapp.model.IChannelList
import kotlinx.android.synthetic.main.fragment_channel_list_item.view.*
import java.util.*

internal class ChannelListAdapter(context: Context,
								  private val channelList: IChannelList,
								  private val listener: Listener) : RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {

	private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
	private val visibleViews = WeakHashMap<ViewHolder, Any>()

	init {
		setHasStableIds(true)
	}

	fun pause() {
		visibleViews.keys.forEach { it.pause() }
	}

	fun resume() {
		notifyDataSetChanged()
		visibleViews.keys.forEach { it.resume() }
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = inflater.inflate(R.layout.fragment_channel_list_item, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val channel = channelList[position]
		holder.setChannel(channel)
	}

	override fun onViewDetachedFromWindow(holder: ViewHolder) {
		holder.pause()
		visibleViews.remove(holder)
	}

	override fun onViewAttachedToWindow(holder: ViewHolder) {
		holder.resume()
		visibleViews[holder] = null
	}

	override fun getItemCount(): Int {
		return channelList.size()
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

		var subtitle: TextView = view.text_channel_subtitle
		var logo: ImageView = view.image_channel_logo
		var programInfoView: ProgramInfoViewBase = view.program_info

		private lateinit var channel: ChannelModel

		init {
			view.setOnLongClickListener(this)
			view.setOnClickListener(this)
		}

		override fun onClick(view: View) {
			listener.onItemClick(channel)
		}

		override fun onLongClick(view: View): Boolean {
			listener.onItemLongClick(channel, view)
			return true
		}

		fun setChannel(channel: ChannelModel) {
			this.channel = channel

			logo.setImageResource(channel.drawableId)
			logo.contentDescription = channel.name

			if (channel.subtitle == null) {
				subtitle.visibility = View.GONE
			} else {
				subtitle.text = channel.subtitle
				subtitle.visibility = View.VISIBLE
			}

			programInfoView.setChannel(channel)
		}

		fun pause() {
			programInfoView.pause()
		}

		fun resume() {
			programInfoView.resume()
		}
	}

	interface Listener {
		fun onItemClick(channel: ChannelModel)

		fun onItemLongClick(channel: ChannelModel, view: View)
	}
}
