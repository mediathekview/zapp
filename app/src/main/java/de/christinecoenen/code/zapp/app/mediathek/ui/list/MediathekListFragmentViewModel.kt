package de.christinecoenen.code.zapp.app.mediathek.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekPagingSource
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class MediathekListFragmentViewModel(
	private val mediathekApi: IMediathekApiService
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
		private const val DEBOUNCE_TIME_MILLIS = 300L
	}

	private val _channelFilter = MediathekChannel.values()
		.map { it to MutableStateFlow(false) }
		.toMap()
	val channelFilter = _channelFilter
		.mapValues { it.value.asLiveData() }

	val isFilterApplied = combine(_channelFilter.values) { channelsSelectors ->
		channelsSelectors.contains(true)
	}.asLiveData()


	// TODO: fix experimental warning
	private val _queryRequest = MutableSharedFlow<QueryRequest>(1)
	val flow = _queryRequest
		.debounce(DEBOUNCE_TIME_MILLIS)
		.flatMapLatest { queryRequest ->
			Pager(PagingConfig(pageSize = ITEM_COUNT_PER_PAGE)) {
				// TODO: check if debouncing works
				MediathekPagingSource(mediathekApi, queryRequest)
			}.flow
		}.cachedIn(viewModelScope)

	private var currentQueryRequest = QueryRequest().apply {
		size = ITEM_COUNT_PER_PAGE
	}

	fun clearFilter() {
		_channelFilter.forEach { it.value.tryEmit(false) }
	}

	fun setChannelFilter(channel: MediathekChannel, isEnabled: Boolean) {
		_channelFilter.getValue(channel).tryEmit(isEnabled)
		currentQueryRequest.setChannel(channel, isEnabled)
		_queryRequest.tryEmit(currentQueryRequest)
	}

	fun setSearchQueryFilter(query: String?) {
		currentQueryRequest.setQueryString(query)
		_queryRequest.tryEmit(currentQueryRequest)
	}
}
