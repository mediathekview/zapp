package de.christinecoenen.code.zapp.app.personal.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.PersonalFragmentLoadStatusItemBinding

class LoadStatusAdapter : RecyclerView.Adapter<LoadStatusViewHolder>() {

	private var isVisible = true
	private var isLoading = true

	@SuppressLint("NotifyDataSetChanged")
	fun onShowsLoaded(showCount: Int) {
		isLoading = false
		isVisible = showCount == 0

		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadStatusViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = PersonalFragmentLoadStatusItemBinding.inflate(layoutInflater, parent, false)
		return LoadStatusViewHolder(binding)
	}

	override fun onBindViewHolder(holder: LoadStatusViewHolder, position: Int) {
	}

	override fun getItemCount() = if (isVisible) 1 else 0
}
