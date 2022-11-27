package de.christinecoenen.code.zapp.app.personal.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModel
import de.christinecoenen.code.zapp.models.shows.SortableMediathekShow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map


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
		.map { pagingData ->

			pagingData
				.map { sortableMediathekShow ->
					UiModel.MediathekShowModel(
						sortableMediathekShow.mediathekShow,
						sortableMediathekShow.sortDate
					)
				}
				.insertSeparators { before, after ->
					// null = no separator
					when {
						after == null -> null
						before == null -> UiModel.DateSeparatorModel(after.localDate)
						!before.isOnSameDay(after) -> UiModel.DateSeparatorModel(after.localDate)
						else -> null
					}
				}

		}
		.asLiveData()
		.cachedIn(viewModelScope)

	fun setSearchQueryFilter(query: String?) {
		_searchQuery.tryEmit(query ?: "")
	}

	protected abstract fun getPagingSource(searchQuery: String): PagingSource<Int, SortableMediathekShow>
}
