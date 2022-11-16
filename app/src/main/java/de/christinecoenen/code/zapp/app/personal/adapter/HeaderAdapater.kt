package de.christinecoenen.code.zapp.app.personal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.PersonalFragmentHeaderItemBinding

class HeaderAdapater(
	@StringRes private val labelResId: Int,
	@DrawableRes private val iconResId: Int,
	private val listener: Listener?,
) : RecyclerView.Adapter<HeaderViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = PersonalFragmentHeaderItemBinding.inflate(layoutInflater, parent, false)

		if (listener == null) {
			binding.more.isVisible = false
		} else {
			binding.more.setOnClickListener {
				listener.onMoreClicked()
			}
		}

		return HeaderViewHolder(binding)
	}

	override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
		holder.bind(labelResId, iconResId)
	}

	override fun getItemCount() = 1

	fun interface Listener {
		fun onMoreClicked()
	}
}
