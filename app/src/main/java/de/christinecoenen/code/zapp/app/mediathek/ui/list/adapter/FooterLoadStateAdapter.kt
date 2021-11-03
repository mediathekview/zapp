package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListItemFooterBinding

class FooterLoadStateAdapter(
	private val retry: () -> Unit
) : LoadStateAdapter<LoadStateViewHolder>() {

	override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) =
		holder.bind(loadState)

	override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding = FragmentMediathekListItemFooterBinding.inflate(inflater, parent, false)
		return LoadStateViewHolder(binding, retry)
	}
}
