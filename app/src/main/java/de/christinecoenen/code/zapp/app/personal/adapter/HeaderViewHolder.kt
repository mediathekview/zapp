package de.christinecoenen.code.zapp.app.personal.adapter

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.PersonalFragmentHeaderItemBinding

class HeaderViewHolder(
	private val binding: PersonalFragmentHeaderItemBinding
) : RecyclerView.ViewHolder(binding.root) {

	fun bind(
		@StringRes titleResId: Int,
		@DrawableRes iconResId: Int,
		showMoreButton: Boolean,
	) {
		binding.title.setText(titleResId)
		binding.icon.setImageResource(iconResId)
		binding.more.isVisible = showMoreButton
	}

}
