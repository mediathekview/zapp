package de.christinecoenen.code.zapp.app.mediathek.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediathekListFragmentViewModel(
	private val mediathekRepository: MediathekRepository
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
	}

	private val _mediathekLoadError = MutableStateFlow<Throwable?>(null)
	val mediathekLoadError = _mediathekLoadError.asLiveData()

	private val _mediathekLoadResult = MutableStateFlow(MediathekLoadResult())
	val mediathekLoadResult = _mediathekLoadResult.asLiveData()

	private val _isLoading = MutableStateFlow(false)
	val isLoading = _isLoading.asLiveData()

	private var getShowsJob: Job? = null
	private var queryRequest = QueryRequest().apply {
		size = ITEM_COUNT_PER_PAGE
	}

	// TODO: debounce
	fun search(query: String?) {
		queryRequest.setSimpleSearch(query)
		loadItems(0, true)
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

	data class MediathekLoadResult(
		val shows: List<MediathekShow> = emptyList(),
		val replaceItems: Boolean = true
	)
}
