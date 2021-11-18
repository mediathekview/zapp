package de.christinecoenen.code.zapp.tv.about

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.TvAboutItemBinding

class AboutViewViewHolder(
	private val binding: TvAboutItemBinding
) : RecyclerView.ViewHolder(binding.root) {

	fun bind(item: AboutItem) {
		binding.title.text = item.getTitle(binding.root.context)
		binding.icon.setImageResource(item.iconResId)
	}

}
