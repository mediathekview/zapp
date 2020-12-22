package de.christinecoenen.code.zapp.app.downloads.ui

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.model.DownloadStatus
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding
import de.christinecoenen.code.zapp.utils.system.ImageHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class DownloadViewHolder(val binding: DownloadsFragmentListItemBinding) :
	RecyclerView.ViewHolder(binding.root) {

	private val disposables = CompositeDisposable()

	fun bindItem(show: PersistedMediathekShow) {
		disposables.clear()

		binding.topic.text = show.mediathekShow.topic
		binding.title.text = show.mediathekShow.title
		binding.duration.text = show.mediathekShow.formattedDuration
		binding.channel.text = show.mediathekShow.channel
		binding.time.text = show.mediathekShow.formattedTimestamp

		when (show.downloadStatus) {
			DownloadStatus.ADDED, DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING -> {
				binding.icon.setImageDrawable(null)
				updatethumbnail(null)
				animateToProgress(show.downloadProgress)
				binding.progressBarAnimated.isVisible = show.downloadProgress == 0
			}
			DownloadStatus.COMPLETED -> {
				binding.icon.setImageDrawable(null)
				loadThumbnail(show)
				binding.progressBar.progress = 0
				binding.progressBarAnimated.isVisible = false
			}
			DownloadStatus.FAILED -> {
				binding.icon.setImageResource(R.drawable.ic_warning_white_24dp)
				updatethumbnail(null)
				binding.progressBar.progress = 0
				binding.progressBarAnimated.isVisible = false
			}
			else -> {
				binding.icon.setImageResource(R.drawable.ic_baseline_help_outline_24)
				updatethumbnail(null)
				binding.progressBar.progress = 0
				binding.progressBarAnimated.isVisible = false
			}
		}
	}

	private fun loadThumbnail(show: PersistedMediathekShow) {
		ImageHelper.loadThumbnailAsync(binding.root.context, show.downloadedVideoPath)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::updatethumbnail, Timber::e)
			.also(disposables::add)
	}

	private fun updatethumbnail(thumbnail: Bitmap?) {
		binding.thumbnail.setImageBitmap(thumbnail)
	}

	private fun animateToProgress(progress: Int) {
		ObjectAnimator.ofInt(binding.progressBar, "progress", progress)
			.apply {
				duration = 500
				interpolator = DecelerateInterpolator()
			}
			.start()
	}
}
