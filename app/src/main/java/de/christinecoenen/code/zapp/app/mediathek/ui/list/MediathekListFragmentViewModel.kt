package de.christinecoenen.code.zapp.app.mediathek.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(FlowPreview::class)
class MediathekListFragmentViewModel(
	private val mediathekRepository: MediathekRepository
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
		private const val DEBOUNCE_TIME_MILLIS = 300L
	}

	private val _mediathekLoadError = MutableStateFlow<Throwable?>(null)
	val mediathekLoadError = _mediathekLoadError.asLiveData()

	private val _mediathekLoadResult = MutableStateFlow(MediathekLoadResult())
	val mediathekLoadResult = _mediathekLoadResult.asLiveData()

	private val _isLoading = MutableStateFlow(true)
	val isLoading = _isLoading.asLiveData()

	private val _channelFilter = MediathekChannel.values()
		.map { it to MutableStateFlow(false) }
		.toMap()
	val channelFilter = _channelFilter
		.mapValues { it.value.asLiveData() }

	val isFilterApplied = combine(_channelFilter.values) { channelsSelectors ->
		channelsSelectors.contains(true)
	}.asLiveData()

	private var getShowsJob: Job? = null
	private var queryRequest = QueryRequest().apply {
		size = ITEM_COUNT_PER_PAGE
	}

	private val _triggerLoadFlow = MutableSharedFlow<Unit>(1)

	init {
		viewModelScope.launch {
			// debounce filter to avoid hitting the api too often when typing
			_triggerLoadFlow.debounce(DEBOUNCE_TIME_MILLIS).collect {
				loadItems(0, true)
			}
		}
	}

	fun clearFilter() {
		_channelFilter.forEach { it.value.tryEmit(false) }
	}

	fun setChannelFilter(channel: MediathekChannel, isEnabled: Boolean) {
		_channelFilter.getValue(channel).tryEmit(isEnabled)
		queryRequest.setChannel(channel, isEnabled)
		_isLoading.tryEmit(true)
		_triggerLoadFlow.tryEmit(Unit)
	}

	fun setSearchQueryFilter(query: String?) {
		queryRequest.setQueryString(query)
		_isLoading.tryEmit(true)
		_triggerLoadFlow.tryEmit(Unit)
	}

	fun loadItems(startWith: Int, replaceItems: Boolean) {
		Timber.d("loadItems: %s", startWith)

		getShowsJob?.cancel()

		queryRequest.offset = startWith

		getShowsJob = viewModelScope.launch(Dispatchers.IO) {
			_isLoading.emit(true)

			try {
				val shows = mediathekRepository.listShows(queryRequest)

				Timber.d("loadItems success; %d shows loaded", shows.size)

				_mediathekLoadResult.emit(MediathekLoadResult(shows, replaceItems))
				_mediathekLoadError.emit(null)
				_isLoading.emit(false)
			} catch (e: Exception) {
				_mediathekLoadError.emit(e)
				_isLoading.emit(false)
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		getShowsJob?.cancel()
	}

	class MediathekLoadResult(
		val shows: List<MediathekShow> = emptyList(),
		val replaceItems: Boolean = true
	)
}
