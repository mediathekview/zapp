package de.christinecoenen.code.zapp.app.settings.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.woxthebox.draglistview.DragItemAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ChannelSelectionFragmentItemBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel

internal class ChannelSelectionAdapter(context: Context) :
	DragItemAdapter<ChannelModel, ChannelSelectionAdapter.ViewHolder>() {

	private val inflater: LayoutInflater =
		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

	init {
		setHasStableIds(true)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = ChannelSelectionFragmentItemBinding.inflate(inflater, parent, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		super.onBindViewHolder(holder, position)

		val channel = mItemList[position]!!
		holder.setChannel(channel)
	}

	override fun getUniqueItemId(position: Int): Long {
		return mItemList[position]!!.id.hashCode().toLong()
	}

	internal class ViewHolder(
		private val binding: ChannelSelectionFragmentItemBinding
	) : DragItemAdapter.ViewHolder(binding.root, R.id.image_handle, false) {

		private var channel: ChannelModel? = null

		fun setChannel(channel: ChannelModel) {
			this.channel = channel

			setVisibility()

			binding.logo.setImageResource(channel.drawableId)
			binding.imageHandle.contentDescription = channel.name

			if (channel.subtitle == null) {
				binding.subtitle.visibility = View.GONE
			} else {
				binding.subtitle.text = channel.subtitle
				binding.subtitle.visibility = View.VISIBLE
			}
		}

		override fun onItemClicked(view: View) {
			channel?.toggleIsEnabled()
			setVisibility()
		}

		private fun setVisibility() {
			val alpha = if (channel?.isEnabled == true) 1f else 0.25f
			binding.logo.alpha = alpha
			binding.subtitle.alpha = alpha

			val handleAlpha = if (channel?.isEnabled == true) 1f else 0.5f
			binding.imageHandle.alpha = handleAlpha
		}
	}
}
