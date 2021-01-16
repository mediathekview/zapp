package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.graphics.Bitmap
import android.view.View
import androidx.core.view.isVisible
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListItemBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.ImageHelper.loadThumbnailAsync
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@KoinApiExtension
internal class MediathekItemViewHolder(
	private val binding: FragmentMediathekListItemBinding
) : BaseViewHolder(binding.root), KoinComponent {

	private val mediathekRepository: MediathekRepository by inject()

	private val disposables = CompositeDisposable()

	fun setShow(show: MediathekShow) {
		disposables.clear()

		binding.imageHolder.visibility = View.GONE
		binding.thumbnail.setImageBitmap(null)
		binding.progress.scaleX = 0f
		binding.title.text = show.title
		binding.topic.text = show.topic
		binding.duration.text = show.formattedDuration
		binding.channel.text = show.channel
		binding.time.text = show.formattedTimestamp
		binding.subtitle.isVisible = show.hasSubtitle
		binding.subtitleDivider.isVisible = show.hasSubtitle

		val persistedShowCall = mediathekRepository
			.getPersistedShowByApiId(show.apiId)

		persistedShowCall
			.filter { it.downloadStatus === DownloadStatus.COMPLETED }
			.firstOrError()
			.flatMap { loadThumbnailAsync(binding.root.context, it.downloadedVideoPath) }
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(::updatethumbnail, Timber::e)
			.also(disposables::add)

		mediathekRepository
			.getPlaybackPositionPercent(show.apiId)
			.filter { it > 0 }
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(::updatePlaybackPosition, Timber::e)
			.also(disposables::add)
	}

	private fun updatePlaybackPosition(progressPercent: Float) {
		binding.progress.scaleX = progressPercent
	}

	private fun updatethumbnail(thumbnail: Bitmap) {
		binding.thumbnail.setImageBitmap(thumbnail)
		binding.imageHolder.visibility = View.VISIBLE
	}
}
