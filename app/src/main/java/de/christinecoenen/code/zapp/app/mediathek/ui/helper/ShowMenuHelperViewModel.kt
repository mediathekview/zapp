package de.christinecoenen.code.zapp.app.mediathek.ui.helper

import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ShowMenuHelperViewModel(
	private val mediathekRepository: MediathekRepository,
	private val downloadController: IDownloadController
) : ViewModel() {

	private val defaultMapping = mapOf(
		R.id.menu_share to true,
		R.id.menu_start_download to true,
		R.id.menu_remove_download to false,
		R.id.menu_cancel_download to false,
		R.id.menu_mark_unwatched to false,
		R.id.menu_add_bookmark to true,
		R.id.menu_remove_bookmark to false
	)

	/**
	 * This will only keep up to date if anyone is consuming
	 * getMenuItemsVisibility.
	 */
	var lastMapping = defaultMapping
		private set

	fun getMenuItemsVisibility(show: MediathekShow): Flow<Map<Int, Boolean>> {
		return mediathekRepository
			.getPersistedShowByApiId(show.apiId)
			.mapLatest {
				lastMapping = mapOf(
					R.id.menu_share to true,
					R.id.menu_start_download to
						(it.downloadStatus in listOf(
							DownloadStatus.NONE,
							DownloadStatus.DELETED,
							DownloadStatus.CANCELLED,
							DownloadStatus.REMOVED,
						)),
					R.id.menu_remove_download to
						(it.downloadStatus in listOf(
							DownloadStatus.FAILED,
							DownloadStatus.COMPLETED,
						)),
					R.id.menu_cancel_download to
						(it.downloadStatus in listOf(
							DownloadStatus.QUEUED,
							DownloadStatus.DOWNLOADING,
							DownloadStatus.PAUSED,
						)),
					R.id.menu_mark_unwatched to (it.playbackPosition > 0),
					R.id.menu_add_bookmark to !it.isBookmarked,
					R.id.menu_remove_bookmark to it.isBookmarked
				)
				lastMapping
			}
			.onStart {
				// for when the show has not yet been persisted
				emit(defaultMapping)
			}
			.distinctUntilChanged()
	}

	suspend fun deleteDownload(show: MediathekShow) {
		mediathekRepository
			.getPersistedShowByApiId(show.apiId)
			.firstOrNull()
			?.let {
				downloadController.deleteDownload(it.id)
			}
	}

	suspend fun cancelDownload(show: MediathekShow) {
		mediathekRepository
			.getPersistedShowByApiId(show.apiId)
			.firstOrNull()
			?.let {
				downloadController.stopDownload(it.id)
			}
	}

	suspend fun markUnwatched(show: MediathekShow) {
		mediathekRepository.resetPlaybackPosition(show.apiId)
	}

	suspend fun bookmark(show: MediathekShow) {
		// we need to persist this first, because it might not yet be persisted!
		mediathekRepository.persistOrUpdateShow(show)
		mediathekRepository.setBookmarked(show.apiId, true)
	}

	suspend fun removeBookmark(show: MediathekShow) {
		mediathekRepository.setBookmarked(show.apiId, false)
	}

	suspend fun startDownload(show: MediathekShow, quality: Quality) {
		// we need to persist this first, because it might not yet be persisted!
		val persistedShow = mediathekRepository.persistOrUpdateShow(show).first()
		downloadController.startDownload(persistedShow.id, quality)
	}
}
