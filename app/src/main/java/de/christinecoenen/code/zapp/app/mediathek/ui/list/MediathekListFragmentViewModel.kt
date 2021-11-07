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
	private val _lengthFilter = MutableStateFlow(Pair<Int?, Int?>(null, null))

	private val _channelFilter = MutableStateFlow(
		MediathekChannel.values()
			.map { it to false }
			.toMap()
			.toMutableMap()
	)
	val channelFilter = _channelFilter.asLiveData()

	val isFilterApplied = combine(_lengthFilter, _channelFilter) { lengthFilter, channelFilter ->
		channelFilter.containsValue(true) || lengthFilter.first != 0 || lengthFilter.second != null
	}
		.asLiveData()

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
				MediathekPagingSource(mediathekApi, queryRequest)
			}.flow
		}.cachedIn(viewModelScope)


	fun clearFilter() {
		val filter = _channelFilter.value.toMutableMap()
		filter.forEach { filter[it.key] = false }
		_channelFilter.tryEmit(filter)
		_lengthFilter.tryEmit(Pair(0, null))
	}

	fun setLengthFilter(minLengthSeconds: Float?, maxLengthSeconds: Float?) {
		_lengthFilter.tryEmit(Pair(minLengthSeconds?.roundToInt(), maxLengthSeconds?.roundToInt()))
	}

	fun setChannelFilter(channel: MediathekChannel, isEnabled: Boolean) {
		val filter = _channelFilter.value.toMutableMap()
		if (filter[channel] != isEnabled) {
			filter[channel] = isEnabled
			_channelFilter.tryEmit(filter)
		}
	}

	fun setSearchQueryFilter(query: String?) {
		_searchQuery.tryEmit(query)
	}

	private fun createQueryRequest(
		searchQuery: String?,
		lengthFilter: Pair<Int?, Int?>,
		channelFilter: Map<MediathekChannel, Boolean>
	): QueryRequest {
		return QueryRequest().apply {
			size = ITEM_COUNT_PER_PAGE
			minDurationSeconds = lengthFilter.first ?: 0
			maxDurationSeconds = lengthFilter.second
			setQueryString(searchQuery)
			channelFilter.onEach { setChannel(it.key, it.value) }
		}
	}
}
