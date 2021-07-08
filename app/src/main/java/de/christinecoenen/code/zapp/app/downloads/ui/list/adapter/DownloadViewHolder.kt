package de.christinecoenen.code.zapp.app.downloads.ui.list.adapter

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.DownloadsFragmentListItemBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.utils.system.ImageHelper
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class DownloadViewHolder(val binding: DownloadsFragmentListItemBinding) :
	RecyclerView.ViewHolder(binding.root) {

	private val disposables = CompositeDisposable()

	fun bindItem(
		show: PersistedMediathekShow,
		showFlowable: Flowable<PersistedMediathekShow>
	) {

		disposables.clear()

		binding.topic.text = show.mediathekShow.topic
		binding.title.text = show.mediathekShow.title
		binding.duration.text = show.mediathekShow.formattedDuration
		binding.channel.text = show.mediathekShow.channel
		binding.time.text = show.mediathekShow.formattedTimestamp

		showFlowable
			.distinctUntilChanged { it -> it.downloadProgress }
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::onDownloadProgressChanged, Timber::e)
			.run(disposables::add)

		showFlowable
			.distinctUntilChanged { it -> it.downloadStatus }
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::onDownloadStatusChanged, Timber::e)
			.run(disposables::add)

		showFlowable
			.map { it.playBackPercent }
			.startWith(0f)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::onPlaybackPositionChanged, Timber::e)
			.run(disposables::add)
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

	private fun onDownloadStatusChanged(show: PersistedMediathekShow) {
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
				binding.icon.setImageResource(R.drawable.ic_warning_white_24dp)
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

	private fun loadThumbnail(show: PersistedMediathekShow) {
		ImageHelper.loadThumbnailAsync(binding.root.context, show.downloadedVideoPath)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::updateThumbnail, Timber::e)
			.run(disposables::add)
	}

	private fun updateThumbnail(thumbnail: Bitmap?) {
		binding.thumbnail.setImageBitmap(thumbnail)
	}

	private fun hideProgess() {
		binding.progressBar.progress = 0
		binding.progressBar.clearAnimation()
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
