package de.christinecoenen.code.zapp.app.downloads.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.flow.Flow


class DownloadsViewModel(
	private val mediathekRepository: MediathekRepository,
	private val downloadController: DownloadController
) : ViewModel() {

	private val pagingConfig = PagingConfig(pageSize = 20)

	val downloadList = Pager(pagingConfig) { mediathekRepository.downloads }
		.liveData
		.cachedIn(viewModelScope)

	fun getPersistedShow(id: Int): Flow<PersistedMediathekShow> {
		return mediathekRepository.getPersistedShow(id)
	}

	suspend fun remove(show: PersistedMediathekShow?) {
		if (show == null) {
			return
		}

		downloadController.deleteDownload(show.id)
		mediathekRepository.updateDownloadStatus(show.downloadId, DownloadStatus.REMOVED)
	}

}
