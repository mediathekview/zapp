package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Adapter between [ChannelViewHolder] and view binding
 * to use the same ViewHolder for app and TV layouts.
 */
interface IChannelViewBindingAdapter {

	val rootView: View
	val logo: ImageView
	val subtitle: TextView
	val showTitle: TextView
	val showSubtitle: TextView
	val showTime: TextView
	val showProgress: ProgressBar
	
}
