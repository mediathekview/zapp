package de.christinecoenen.code.zapp.tv2.mediathek

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekPagingSource
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MediathekScreenViewModel(
	private val mediathekApi: IMediathekApiService,
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
		private const val DEBOUNCE_TIME_MILLIS = 300L
	}

	private val pagingConfig = PagingConfig(
		pageSize = ITEM_COUNT_PER_PAGE,
		enablePlaceholders = false
	)


	private val _searchQuery = MutableStateFlow("")

	private val _queryInfoResult = MutableStateFlow<QueryInfoResult?>(null)

	val shows
		get() = _searchQuery
			.mapLatest { searchQuery ->
				createQueryRequest(searchQuery)
			}
			.debounce(DEBOUNCE_TIME_MILLIS)
			.flatMapLatest { queryRequest ->
				Pager(pagingConfig) {
					MediathekPagingSource(mediathekApi, queryRequest, _queryInfoResult)
				}.flow
			}
			.cachedIn(viewModelScope)

	fun loadShows(searchQuery: String) {
		_searchQuery.value = searchQuery
	}

	private fun createQueryRequest(searchQuery: String): QueryRequest {
		return QueryRequest().apply {
			size = ITEM_COUNT_PER_PAGE
			future = false
			setQueryString(searchQuery)
		}
	}
}
