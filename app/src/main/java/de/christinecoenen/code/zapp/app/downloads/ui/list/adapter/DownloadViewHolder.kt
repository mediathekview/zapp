package de.christinecoenen.code.zapp.app.downloads.ui.list.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.ImageHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class DownloadViewHolder(
	val binding: DownloadsFragmentListItemBinding
) : RecyclerView.ViewHolder(binding.root), KoinComponent {

	private val mediathekRepository: MediathekRepository by inject()

	private var downloadProgressJob: Job? = null
	private var downloadStatusJob: Job? = null
	private var playbackPositionJob: Job? = null
	private var videoPathJob: Job? = null

	private var showt: PersistedMediathekShow? = null

	suspend fun bindItem(show: PersistedMediathekShow) = withContext(Dispatchers.Main) {
		binding.root.visibility = View.GONE

		showt = show

		downloadProgressJob?.cancel()
		downloadStatusJob?.cancel()
		playbackPositionJob?.cancel()
		videoPathJob?.cancel()

		binding.topic.text = show.mediathekShow.topic
		binding.title.text = show.mediathekShow.title
		binding.duration.text = show.mediathekShow.formattedDuration
		binding.channel.text = show.mediathekShow.channel
		binding.time.text = show.mediathekShow.formattedTimestamp

		binding.downloadProgress.isVisible = false
		binding.downloadProgress.progress = 0
		binding.icon.setImageDrawable(null)
		updateThumbnail(null)

		binding.root.visibility = View.VISIBLE

		downloadProgressJob = launch { updateDownloadProgressFlow(show) }
		downloadStatusJob = launch { updateDownloadStatusFlow(show) }
		playbackPositionJob = launch { updatePlaybackPositionPercentFlow(show) }
		videoPathJob = launch { getCompletetlyDownloadedVideoPathFlow(show) }
	}

	private suspend fun updateDownloadProgressFlow(show: PersistedMediathekShow) {
		mediathekRepository
			.getDownloadProgress(show.id)
			.collectLatest(::onDownloadProgressChanged)
	}

	private suspend fun updateDownloadStatusFlow(show: PersistedMediathekShow) {
		mediathekRepository
			.getDownloadStatus(show.id)
			.collectLatest(::onDownloadStatusChanged)
	}

	private suspend fun updatePlaybackPositionPercentFlow(show: PersistedMediathekShow) {
		mediathekRepository
			.getPlaybackPositionPercent(show.mediathekShow.apiId)
			.collectLatest(::onPlaybackPositionChanged)
	}

	private suspend fun getCompletetlyDownloadedVideoPathFlow(show: PersistedMediathekShow) {
		mediathekRepository
			.getCompletetlyDownloadedVideoPath(show.id)
			.collectLatest(::onVideoPathChanged)
	}

	private fun onDownloadProgressChanged(downloadProgress: Int) {
		binding.downloadProgress.progress = downloadProgress
	}

	private fun onDownloadStatusChanged(status: DownloadStatus) {
		when (status) {
			DownloadStatus.ADDED, DownloadStatus.QUEUED -> {
				binding.icon.setImageDrawable(null)
			}
			DownloadStatus.DOWNLOADING -> {
				binding.icon.setImageDrawable(null)
			}
			DownloadStatus.COMPLETED -> {
				binding.icon.setImageDrawable(null)
			}
			DownloadStatus.FAILED -> {
				binding.icon.setImageResource(R.drawable.ic_outline_warning_amber_24)
			}
			else -> {
				binding.icon.setImageResource(R.drawable.ic_baseline_help_outline_24)
			}
		}

		binding.downloadProgress.isVisible = status == DownloadStatus.QUEUED ||
			status == DownloadStatus.DOWNLOADING ||
			status == DownloadStatus.PAUSED ||
			status == DownloadStatus.ADDED

		binding.downloadProgress.isIndeterminate = status != DownloadStatus.DOWNLOADING
	}

	private fun onPlaybackPositionChanged(playBackPercent: Float) {
		binding.viewingProgress.scaleX = playBackPercent
	}

	private suspend fun onVideoPathChanged(videoPath: String?) {
		loadThumbnail(videoPath)
	}

	private suspend fun loadThumbnail(path: String?) = coroutineScope {
		if (path == null) {
			updateThumbnail(null)
			return@coroutineScope
		}

		try {
			val thumbnail =
				ImageHelper.loadThumbnailAsync(binding.root.context, path)
			updateThumbnail(thumbnail)

		} catch (e: Exception) {
			onLoadThumbnailError()
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
}
