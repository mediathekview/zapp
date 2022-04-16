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
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import de.christinecoenen.code.zapp.app.mediathek.ui.list.models.ChannelFilter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.models.LengthFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.math.roundToInt

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MediathekListFragmentViewModel(
	private val mediathekApi: IMediathekApiService
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
		private const val DEBOUNCE_TIME_MILLIS = 300L
	}

	private val _searchQuery = MutableStateFlow<String?>(null)

	private val _lengthFilter = MutableStateFlow(LengthFilter())
	val lengthFilter = _lengthFilter.asLiveData()

	private val _channelFilter = MutableStateFlow(ChannelFilter())
	val channelFilter = _channelFilter.asLiveData()

	val isFilterApplied = combine(_lengthFilter, _channelFilter) { lengthFilter, channelFilter ->
		channelFilter.isApplied || lengthFilter.isApplied
	}
		.asLiveData()

	private val _queryInfoResult = MutableStateFlow<QueryInfoResult?>(null)
	val queryInfoResult = _queryInfoResult.asLiveData()

	val pageFlow = combine(
		_searchQuery,
		_lengthFilter,
		_channelFilter
	) { searchQuery, lengthFilter, channelFilter ->
		createQueryRequest(searchQuery, lengthFilter, channelFilter)
	}
		.debounce(DEBOUNCE_TIME_MILLIS)
		.flatMapLatest { queryRequest ->
			Pager(PagingConfig(pageSize = ITEM_COUNT_PER_PAGE)) {
				MediathekPagingSource(mediathekApi, queryRequest, _queryInfoResult)
			}.flow
		}.cachedIn(viewModelScope)


	fun clearFilter() {
		_channelFilter.tryEmit(ChannelFilter())
		_lengthFilter.tryEmit(LengthFilter())
	}

	fun setLengthFilter(minLengthSeconds: Float?, maxLengthSeconds: Float?) {
		val min = minLengthSeconds?.roundToInt() ?: 0
		val max = maxLengthSeconds?.roundToInt()
		_lengthFilter.tryEmit(LengthFilter(min, max))
	}

	fun setChannelFilter(channel: MediathekChannel, isEnabled: Boolean) {
		val filter = _channelFilter.value.copy()
		val hasChanged = filter.setEnabled(channel, isEnabled)
		if (hasChanged) {
			_channelFilter.tryEmit(filter)
		}
	}

	fun setSearchQueryFilter(query: String?) {
		_searchQuery.tryEmit(query)
	}

	private fun createQueryRequest(
		searchQuery: String?,
		lengthFilter: LengthFilter,
		channelFilter: ChannelFilter
	): QueryRequest {
		return QueryRequest().apply {
			size = ITEM_COUNT_PER_PAGE
			minDurationSeconds = lengthFilter.minDurationSeconds
			maxDurationSeconds = lengthFilter.maxDurationSeconds
			setQueryString(searchQuery)
			for (filterItem in channelFilter) {
				setChannel(filterItem.key, filterItem.value)
			}
		}
	}
}
