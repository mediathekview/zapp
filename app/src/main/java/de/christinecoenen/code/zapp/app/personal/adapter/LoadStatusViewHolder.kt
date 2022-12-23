package de.christinecoenen.code.zapp.app.personal.adapter

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.PersonalFragmentLoadStatusItemBinding

class LoadStatusViewHolder(
	private val binding: PersonalFragmentLoadStatusItemBinding
) : RecyclerView.ViewHolder(binding.root) {

	fun bind(@StringRes titleResId: Int) {
		binding.title.setText(titleResId)
	}

}
