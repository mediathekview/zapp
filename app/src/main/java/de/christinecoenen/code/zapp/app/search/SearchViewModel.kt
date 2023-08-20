package de.christinecoenen.code.zapp.app.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModel
import de.christinecoenen.code.zapp.models.shows.SortableMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(private val mediathekRepository: MediathekRepository) : ViewModel() {
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
