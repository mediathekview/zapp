package de.christinecoenen.code.zapp.app.personal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.PersonalFragmentHeaderItemBinding

class HeaderAdapater(
	@StringRes private val labelResId: Int,
	@DrawableRes private val iconResId: Int,
	private val listener: Listener?,
) : RecyclerView.Adapter<HeaderViewHolder>() {

	private var showMoreButton = false
	private var isVisible = true

	fun setIsVisible(isVisible: Boolean) {
		if (this.isVisible == isVisible) {
			return
		}

		this.isVisible = isVisible

		if (isVisible) {
			notifyItemInserted(0)
		} else {
			notifyItemRemoved(0)
		}
	}

	fun setShowMoreButton(showMoreButton: Boolean) {
		if (this.showMoreButton == showMoreButton) {
			return
		}

		this.showMoreButton = showMoreButton
		notifyItemChanged(1)
	}

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

		holder.itemView.apply {
			val marginTop = resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)
			updateLayoutParams<RecyclerView.LayoutParams> {
				topMargin = if (position == 0) 0 else marginTop
			}
		}

		holder.bind(labelResId, iconResId, showMoreButton)
	}

	override fun getItemCount() = if (isVisible) 1 else 0

	fun interface Listener {
		fun onMoreClicked()
	}
}
