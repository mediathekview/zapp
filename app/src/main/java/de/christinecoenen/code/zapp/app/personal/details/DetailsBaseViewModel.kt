package de.christinecoenen.code.zapp.app.personal.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModel
import de.christinecoenen.code.zapp.models.shows.SortableMediathekShow
import kotlinx.coroutines.flow.map

abstract class DetailsBaseViewModel : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
	}

	private val pagingConfig = PagingConfig(
		pageSize = ITEM_COUNT_PER_PAGE,
		enablePlaceholders = false
	)

	val showList = Pager(pagingConfig) { getPagingSource() }.flow
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

	protected abstract fun getPagingSource(): PagingSource<Int, SortableMediathekShow>
}
