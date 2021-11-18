package de.christinecoenen.code.zapp.tv.about

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.TvAboutItemBinding

class AboutViewViewHolder(
	private val binding: TvAboutItemBinding,
	val listener: AboutItemListener
) : RecyclerView.ViewHolder(binding.root) {

	private var item: AboutItem? = null

	init {
		binding.root.setOnClickListener {
			item?.let { listener.onclick(it) }
		}
	}

	fun bind(item: AboutItem) {
		this.item = item
		binding.title.text = item.getTitle(binding.root.context)
		binding.icon.setImageResource(item.iconResId)
	}

}
