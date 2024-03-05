package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemFooterBinding
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException

class LoadStateViewHolder(
	private val binding: MediathekListFragmentItemFooterBinding,
	private val showErrors: Boolean,
	private val retry: (() -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.retryButton.setOnClickListener { retry?.invoke() }
	}

	fun bind(loadState: LoadState) {
		binding.errorMessage.isVisible = loadState is LoadState.Error && showErrors
		binding.retryButton.isVisible = loadState is LoadState.Error && showErrors && retry != null
		binding.progress.isVisible = loadState is LoadState.Loading

		if (loadState is LoadState.Error) {
			val errorMessageResId =
				if (loadState.error is SSLHandshakeException || loadState.error is UnknownServiceException) {
					R.string.error_mediathek_ssl_error
				} else {
					R.string.error_mediathek_info_not_available
				}

			binding.errorMessage.setText(errorMessageResId)
		}
	}

}
