package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.animation.LayoutTransition
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.ColorHelper.themeColor
import de.christinecoenen.code.zapp.utils.system.ImageHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.transform
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

class MediathekItemViewHolder(
	private val binding: MediathekListFragmentItemBinding,
	private val highlightRelevantForUser: Boolean,
	private val scope: LifecycleCoroutineScope,
) : RecyclerView.ViewHolder(binding.root), KoinComponent {

	private val mediathekRepository: MediathekRepository by inject()

	private val bgColorDefault = binding.root.context.themeColor(com.google.android.material.R.attr.backgroundColor)
	private val bgColorHighlight by lazy { binding.root.context.themeColor(com.google.android.material.R.attr.colorSurface) }

	private var isRelevantForUserJob: Job? = null
	private var isBookmarkedJob: Job? = null
	private var downloadProgressJob: Job? = null
	private var downloadStatusJob: Job? = null
	private var playbackPositionJob: Job? = null
	private var videoPathJob: Job? = null

	fun setShow(show: MediathekShow) {
		recycle()

		binding.title.text = show.title
		// fix max lines not applied correctly
		binding.title.requestLayout()

		binding.topic.text = show.topic
		// fix layout_constraintWidth_max not be applied correctly
		binding.topic.requestLayout()

		binding.duration.text = show.formattedDuration
		binding.channel.text = show.channel
		binding.time.text = show.formattedTimestamp
		binding.subtitle.isVisible = show.hasSubtitle
		binding.subtitleDivider.isVisible = show.hasSubtitle

		binding.bookmarkIcon.isVisible = false
		binding.downloadProgress.isVisible = false
		binding.downloadProgressIcon.isVisible = false
		binding.downloadStatusIcon.isVisible = false
		binding.viewingStatus.isVisible = false
		binding.viewingProgress.isVisible = false

		binding.root.setBackgroundColor(bgColorDefault)

		if (highlightRelevantForUser) {
			isRelevantForUserJob = scope.launch { getIsRelevantForUserFlow(show) }
		}

		isBookmarkedJob = scope.launch { isBookmarkedFlow(show) }
		videoPathJob = scope.launch { getCompletetlyDownloadedVideoPathFlow(show) }
		downloadProgressJob = scope.launch { updateDownloadProgressFlow(show) }
		playbackPositionJob = scope.launch { updatePlaybackPositionPercentFlow(show) }
		downloadStatusJob = scope.launch { updateDownloadStatusFlow(show) }

		binding.root.layoutTransition = LayoutTransition()
		binding.root.layoutTransition.disableTransitionType(LayoutTransition.CHANGE_APPEARING)
		binding.root.layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
	}

	fun recycle() {
		isRelevantForUserJob?.cancel()
		isBookmarkedJob?.cancel()
		downloadProgressJob?.cancel()
		downloadStatusJob?.cancel()
		playbackPositionJob?.cancel()
		videoPathJob?.cancel()

		updateThumbnail(null)

		binding.root.layoutTransition = null
	}

	private suspend fun getIsRelevantForUserFlow(show: MediathekShow) {
		mediathekRepository
			.getIsRelevantForUser(show.apiId)
			.collectLatest(::updateIsRelevantForUser)
	}

	private suspend fun isBookmarkedFlow(show: MediathekShow) {
		mediathekRepository
			.getIsBookmarked(show.apiId)
			.collectLatest(::updateIsBookmarked)
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

	private suspend fun updatePlaybackPositionPercentFlow(show: MediathekShow) {
		mediathekRepository
			.getPlaybackPositionPercent(show.apiId)
			.collectLatest(::updatePlaybackPositionPercentFlow)
	}

	private suspend fun getCompletetlyDownloadedVideoPathFlow(show: MediathekShow) {
		mediathekRepository
			.getCompletetlyDownloadedVideoPath(show.apiId)
			.transform {
				emit(it)
				delay(500.milliseconds)
			}
			.collectLatest(::onVideoPathChanged)
	}

	private fun updateIsRelevantForUser(isRelevant: Boolean) {
		binding.root.setBackgroundColor(if (isRelevant) bgColorHighlight else bgColorDefault)
	}

	private fun updateIsBookmarked(isBookmarked: Boolean) {
		binding.bookmarkIcon.isVisible = isBookmarked
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
	}

	private fun updatePlaybackPositionPercentFlow(percent: Float) {
		binding.viewingStatus.isVisible = percent > 0
		binding.viewingProgress.progress = (percent * binding.viewingProgress.max).toInt()
		binding.viewingProgress.isVisible = percent > 0
	}

	private suspend fun onVideoPathChanged(videoPath: String?) {
		loadThumbnail(videoPath)
	}

	private suspend fun loadThumbnail(path: String?) = coroutineScope {
		if (path == null) {
			binding.thumbnail.isVisible = false
			return@coroutineScope
		}

		binding.thumbnail.isVisible = true

		try {
			val thumbnail = ImageHelper.loadThumbnailAsync(binding.root.context, path)
			updateThumbnail(thumbnail)
		} catch (e: CancellationException) {
			// this is fine - view will be recycled
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
		binding.thumbnail.isVisible = thumbnail != null
		binding.thumbnail.setImageBitmap(thumbnail)
		binding.thumbnail.imageAlpha = 255
		binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
	}
}
