package de.christinecoenen.code.zapp.app.mediathek.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.models.ChannelFilter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.models.LengthFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

class MediathekFilterViewModel : ViewModel() {

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	private val _lengthFilter = MutableStateFlow(LengthFilter())
	val lengthFilter = _lengthFilter.asStateFlow()

	private val _channelFilter = MutableStateFlow(ChannelFilter())
	val channelFilter = _channelFilter.asStateFlow()

	val isFilterApplied = combine(_lengthFilter, _channelFilter) { lengthFilter, channelFilter ->
		channelFilter.isApplied || lengthFilter.isApplied
	}
		.asLiveData()

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
		_searchQuery.tryEmit(query ?: "")
	}
}
