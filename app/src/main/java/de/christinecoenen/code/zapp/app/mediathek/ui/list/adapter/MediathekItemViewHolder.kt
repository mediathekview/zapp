package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.ColorHelper.themeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MediathekItemViewHolder(
	private val binding: MediathekListFragmentItemBinding
) : RecyclerView.ViewHolder(binding.root), KoinComponent {

	private val mediathekRepository: MediathekRepository by inject()

	private val bgColorDefault = binding.root.context.themeColor(R.attr.backgroundColor)
	private val bgColorHighlight by lazy { binding.root.context.themeColor(R.attr.colorSurface) }

	private var downloadProgressJob: Job? = null
	private var downloadStatusJob: Job? = null
	private var playbackPositionJob: Job? = null

	suspend fun setShow(show: MediathekShow) = withContext(Dispatchers.Main) {
		binding.root.visibility = View.GONE

		downloadProgressJob?.cancel()
		downloadStatusJob?.cancel()
		playbackPositionJob?.cancel()

		binding.title.text = show.title
		binding.topic.text = show.topic
		// fix layout_constraintWidth_max not be applied correctly
		binding.topic.requestLayout()

		binding.duration.text = show.formattedDuration
		binding.channel.text = show.channel
		binding.time.text = show.formattedTimestamp
		binding.subtitle.isVisible = show.hasSubtitle
		binding.subtitleDivider.isVisible = show.hasSubtitle

		binding.downloadProgress.isVisible = false
		binding.downloadProgressIcon.isVisible = false
		binding.downloadStatusIcon.isVisible = false
		binding.viewingStatus.isVisible = false
		binding.viewingProgress.isVisible = false

		binding.root.setBackgroundColor(bgColorDefault)

		binding.root.visibility = View.VISIBLE

		downloadProgressJob = launch { updateDownloadProgressFlow(show) }
		downloadStatusJob = launch { updateDownloadStatusFlow(show) }
		playbackPositionJob = launch { updatePlaybackPositionPercent(show) }
	}

	private suspend fun updateDownloadProgressFlow(show: MediathekShow) {
		mediathekRepository
			.getDownloadProgress(show.apiId)
			.collectLatest(::updateDownloadProgress)
	}

	private suspend fun updateDownloadStatusFlow(show: MediathekShow) {
		mediathekRepository
			.getDownloadStatus(show.apiId)
			.collectLatest(::updateDownloadStatus)
	}

	private suspend fun updatePlaybackPositionPercent(show: MediathekShow) {
		mediathekRepository
			.getPlaybackPositionPercent(show.apiId)
			.collectLatest(::updatePlaybackPositionPercent)
	}

	private fun updateDownloadProgress(progress: Int) {
		binding.downloadProgress.progress = progress
	}

	private fun updateDownloadStatus(status: DownloadStatus) {
		binding.downloadStatusIcon.isVisible = status == DownloadStatus.FAILED ||
			status == DownloadStatus.COMPLETED

		binding.downloadStatusIcon.setImageResource(
			when (status) {
				DownloadStatus.COMPLETED -> R.drawable.ic_baseline_save_alt_24
				DownloadStatus.FAILED -> R.drawable.ic_outline_warning_amber_24
				else -> 0
			}
		)

		binding.downloadProgress.isVisible = status == DownloadStatus.QUEUED ||
			status == DownloadStatus.DOWNLOADING ||
			status == DownloadStatus.PAUSED ||
			status == DownloadStatus.ADDED
		binding.downloadProgressIcon.isVisible = binding.downloadProgress.isVisible

		binding.downloadProgress.isIndeterminate = status != DownloadStatus.DOWNLOADING

		binding.root.setBackgroundColor(
			when (status) {
				DownloadStatus.NONE,
				DownloadStatus.CANCELLED,
				DownloadStatus.REMOVED,
				DownloadStatus.DELETED -> bgColorDefault
				else -> bgColorHighlight
			}
		)
	}

	private fun updatePlaybackPositionPercent(percent: Float) {
		binding.viewingStatus.isVisible = percent > 0
		binding.viewingProgress.progress = (percent * binding.viewingProgress.max).toInt()
		binding.viewingProgress.isVisible = percent > 0
	}
}
