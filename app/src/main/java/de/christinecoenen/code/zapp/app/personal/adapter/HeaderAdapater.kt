package de.christinecoenen.code.zapp.app.personal.adapter

import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class HeaderAdapater(
	@StringRes private val labelResId: Int
) : RecyclerView.Adapter<HeaderViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
		return HeaderViewHolder(MaterialTextView(parent.context))
	}

	override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
		(holder.itemView as MaterialTextView).setText(labelResId)
	}

	override fun getItemCount() = 1
}
