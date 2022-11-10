package de.christinecoenen.code.zapp.app.personal.adapter

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.PersonalFragmentHeaderItemBinding

class HeaderViewHolder(
	private val binding: PersonalFragmentHeaderItemBinding
) : RecyclerView.ViewHolder(binding.root) {

	fun bind(
		@StringRes titleResId: Int,
		@DrawableRes iconResId: Int,
	) {
		binding.title.setText(titleResId)
		binding.icon.setImageResource(iconResId)
	}

}
