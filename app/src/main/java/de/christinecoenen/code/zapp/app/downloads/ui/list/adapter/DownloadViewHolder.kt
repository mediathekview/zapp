package de.christinecoenen.code.zapp.app.downloads.ui.list.adapter

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.utils.system.ImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber


class DownloadViewHolder(val binding: DownloadsFragmentListItemBinding) :
	RecyclerView.ViewHolder(binding.root) {

	private var loadThumbnailJob: Job? = null
	private var downloadProgressJob: Job? = null
	private var downloadStatusJob: Job? = null
	private var playbackPositionJob: Job? = null

	suspend fun bindItem(
		show: PersistedMediathekShow,
		showFlow: Flow<PersistedMediathekShow>
	) {
		binding.root.visibility = View.GONE

		loadThumbnailJob?.cancel()
		downloadProgressJob?.cancel()
		downloadStatusJob?.cancel()
		playbackPositionJob?.cancel()

		binding.topic.text = show.mediathekShow.topic
		binding.title.text = show.mediathekShow.title
		binding.duration.text = show.mediathekShow.formattedDuration
		binding.channel.text = show.mediathekShow.channel
		binding.time.text = show.mediathekShow.formattedTimestamp

		binding.progressBarAnimated.isVisible = false
		binding.progressBar.isVisible = false
		binding.icon.setImageDrawable(null)

		binding.root.visibility = View.VISIBLE

		coroutineScope {

			downloadProgressJob = launch(Dispatchers.Main) {
				showFlow
					.distinctUntilChangedBy { it.downloadProgress }
					.catch { exception -> Timber.e(exception) }
					.collect(::onDownloadProgressChanged)
			}

			downloadStatusJob = launch(Dispatchers.Main) {
				showFlow
					.distinctUntilChangedBy { it.downloadStatus }
					.catch { exception -> Timber.e(exception) }
					.collect(::onDownloadStatusChanged)
			}

			playbackPositionJob = launch(Dispatchers.Main) {
				showFlow
					.map { it.playBackPercent }
					.onStart { emit(0f) }
					.catch { exception -> Timber.e(exception) }
					.collect(::onPlaybackPositionChanged)
			}
		}
	}

	private fun onDownloadProgressChanged(show: PersistedMediathekShow) {
		when (show.downloadStatus) {
			DownloadStatus.DOWNLOADING -> {
				animateToProgress(show.downloadProgress)
				setProgressBarVisibilityDuringDownload(show.downloadProgress)
			}
			else -> {
			}
		}
	}

	private suspend fun onDownloadStatusChanged(show: PersistedMediathekShow) {
		when (show.downloadStatus) {
			DownloadStatus.ADDED, DownloadStatus.QUEUED -> {
				binding.icon.setImageDrawable(null)
				updateThumbnail(null)
				binding.progressBarAnimated.isVisible = true
				binding.progressBar.isVisible = false
			}
			DownloadStatus.DOWNLOADING -> {
				binding.icon.setImageDrawable(null)
				updateThumbnail(null)
				setProgressBarVisibilityDuringDownload(show.downloadProgress)
				binding.progressBar.progress = show.downloadProgress
			}
			DownloadStatus.COMPLETED -> {
				binding.icon.setImageDrawable(null)
				loadThumbnail(show)
				hideProgess()
				binding.progressBarAnimated.isVisible = false
			}
			DownloadStatus.FAILED -> {
				binding.icon.setImageResource(R.drawable.ic_outline_warning_amber_24)
				updateThumbnail(null)
				hideProgess()
				binding.progressBarAnimated.isVisible = false
			}
			else -> {
				binding.icon.setImageResource(R.drawable.ic_baseline_help_outline_24)
				updateThumbnail(null)
				hideProgess()
				binding.progressBarAnimated.isVisible = false
			}
		}
	}

	private fun onPlaybackPositionChanged(playBackPercent: Float) {
		binding.viewingProgress.scaleX = playBackPercent
	}

	private suspend fun loadThumbnail(show: PersistedMediathekShow) = coroutineScope {
		loadThumbnailJob = launch(Dispatchers.Main) {
			try {
				val thumbnail =
					ImageHelper.loadThumbnailAsync(binding.root.context, show.downloadedVideoPath)
				updateThumbnail(thumbnail)

			} catch (e: Exception) {
				onLoadThumbnailError()
			}
		}
	}

	private fun onLoadThumbnailError() {
		binding.thumbnail.setImageResource(R.drawable.ic_sad_tv)
		binding.thumbnail.imageAlpha = 28
		binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE
	}

	private fun updateThumbnail(thumbnail: Bitmap?) {
		binding.thumbnail.setImageBitmap(thumbnail)
		binding.thumbnail.imageAlpha = 255
		binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
	}

	private fun hideProgess() {
		binding.progressBar.progress = 0
		binding.progressBar.clearAnimation()
		binding.progressBar.isVisible = false
	}

	private fun setProgressBarVisibilityDuringDownload(progress: Int) {
		binding.progressBarAnimated.isVisible = progress == 0
		binding.progressBar.isVisible = !binding.progressBarAnimated.isVisible
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
