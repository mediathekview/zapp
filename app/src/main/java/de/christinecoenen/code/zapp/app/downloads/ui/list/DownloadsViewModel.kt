package de.christinecoenen.code.zapp.app.downloads.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest


@OptIn(ExperimentalCoroutinesApi::class)
class DownloadsViewModel(
	private val mediathekRepository: MediathekRepository,
	private val downloadController: IDownloadController
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
	}

	private val pagingConfig = PagingConfig(pageSize = ITEM_COUNT_PER_PAGE)

	private val _searchQuery = MutableStateFlow("")

	val downloadList = _searchQuery
		.flatMapLatest { searchQuery ->
			Pager(pagingConfig) {
				mediathekRepository.getDownloads(searchQuery)
			}.flow
		}
		.asLiveData()
		.cachedIn(viewModelScope)

	suspend fun remove(show: MediathekShow?) {
		if (show == null) {
			return
		}

		mediathekRepository
			.getPersistedShowByApiId(show.apiId)
			.firstOrNull()
			?.let {
				downloadController.deleteDownload(it.id)
				mediathekRepository.updateDownloadStatus(it.downloadId, DownloadStatus.REMOVED)
			}
	}

	fun setSearchQueryFilter(query: String?) {
		_searchQuery.tryEmit(query ?: "")
	}
}
