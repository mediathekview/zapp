package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemFooterBinding

class MediathekLoadStateAdapter(
	private val showErrors: Boolean = true,
	private val retry: (() -> Unit)? = null,
) : LoadStateAdapter<LoadStateViewHolder>() {

	override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) =
		holder.bind(loadState)

	override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding = MediathekListFragmentItemFooterBinding.inflate(inflater, parent, false)
		return LoadStateViewHolder(binding, showErrors, retry)
	}
}
