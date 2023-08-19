package de.christinecoenen.code.zapp.app.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(private val mediathekRepository: MediathekRepository) : ViewModel() {
	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
	}

	private val pagingConfig = PagingConfig(
		pageSize = ITEM_COUNT_PER_PAGE,
		enablePlaceholders = false
	)

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	private val _isSubmitted = MutableStateFlow(false)
	val isSubmitted = _isSubmitted.asStateFlow()

	val localSearchSuggestions = _searchQuery
		.flatMapLatest { query ->
			if (query.isEmpty()) {
				flowOf(PagingData.empty())
			} else {
				Pager(pagingConfig) { mediathekRepository.getLocalSearchSuggestions(query) }.flow
			}
		}
		.cachedIn(viewModelScope)

	fun setSearchQuery(query: String?) {
		_isSubmitted.tryEmit(false)
		_searchQuery.tryEmit(query ?: "")
	}

	fun submit() {
		_isSubmitted.tryEmit(true)
		// TODO: save current query
	}
}
