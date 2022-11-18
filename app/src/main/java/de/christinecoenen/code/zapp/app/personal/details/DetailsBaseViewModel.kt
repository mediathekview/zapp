package de.christinecoenen.code.zapp.app.personal.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest


@OptIn(ExperimentalCoroutinesApi::class)
abstract class DetailsBaseViewModel() : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
	}

	private val pagingConfig = PagingConfig(pageSize = ITEM_COUNT_PER_PAGE)

	private val _searchQuery = MutableStateFlow("")

	val showList = _searchQuery
		.flatMapLatest { searchQuery ->
			Pager(pagingConfig) { getPagingSource(searchQuery) }.flow
		}
		.asLiveData()
		.cachedIn(viewModelScope)

	fun setSearchQueryFilter(query: String?) {
		_searchQuery.tryEmit(query ?: "")
	}

	protected abstract fun getPagingSource(searchQuery: String): PagingSource<Int, PersistedMediathekShow>
}
