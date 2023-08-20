package de.christinecoenen.code.zapp.app.search

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekPagingSource
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModel
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.SortableMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.joda.time.DateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
	private val mediathekRepository: MediathekRepository,
	private val mediathekApi: IMediathekApiService
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
	}

	enum class SeachState {
		None,
		Query,
		Results
	}

	private val pagingConfig = PagingConfig(
		pageSize = ITEM_COUNT_PER_PAGE,
		enablePlaceholders = false
	)

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	private val _searchState = MutableStateFlow(SeachState.None)
	val searchState = _searchState.asStateFlow()

	val localSearchSuggestions = _searchQuery
		.flatMapLatest { query ->
			if (query.isEmpty()) {
				flowOf(PagingData.empty())
			} else {
				Pager(pagingConfig) { mediathekRepository.getLocalSearchSuggestions(query) }.flow
			}
		}
		.cachedIn(viewModelScope)

	val localShowsResult = _searchQuery
		.flatMapLatest { query ->
			if (query.isEmpty()) {
				flowOf(PagingData.empty())
			} else {
				// TODO: fetch bookmarks and other local shows too
				Pager(pagingConfig) { mediathekRepository.getDownloads(query) }.flow
			}
		}
		.map<PagingData<SortableMediathekShow>, PagingData<UiModel>> { pagingData ->
			pagingData.map { show ->
				UiModel.MediathekShowModel(show.mediathekShow, show.sortDate)
			}
		}
		.cachedIn(viewModelScope)

	private val _mediathekResultInfo = MutableStateFlow<QueryInfoResult?>(null)
	val mediathekResultInfo = _mediathekResultInfo.asLiveData()

	val mediathekResult = _searchQuery
		.map { query ->
			QueryRequest().apply {
				size = ITEM_COUNT_PER_PAGE
				setQueryString(query)
			}
		}
		.flatMapLatest { queryRequest ->
			Pager(pagingConfig) {
				MediathekPagingSource(mediathekApi, queryRequest, _mediathekResultInfo)
			}.flow
		}
		.map<PagingData<MediathekShow>, PagingData<UiModel>> { pagingData ->
			pagingData.map { show ->
				UiModel.MediathekShowModel(
					show,
					DateTime(show.timestamp.toLong() * DateUtils.SECOND_IN_MILLIS)
				)
			}
		}
		.cachedIn(viewModelScope)

	fun setSearchQuery(query: String?) {
		_searchState.tryEmit(SeachState.Query)
		_searchQuery.tryEmit(query ?: "")
	}

	fun submit() {
		_searchState.tryEmit(SeachState.Results)
		// TODO: save current query
	}

	fun enterQueryMode() {
		_searchState.tryEmit(SeachState.Query)
	}

	fun exit() {
		_searchState.tryEmit(SeachState.None)
	}
}
