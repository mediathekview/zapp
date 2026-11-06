package de.christinecoenen.code.zapp.app.personal.series

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import de.christinecoenen.code.zapp.repositories.ShowCollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SeriesViewModel(
	private val showCollectionRepository: ShowCollectionRepository
) : ViewModel() {

	val collectionsFlow = showCollectionRepository.getAllWithCounts()

	private val _markResult = MutableStateFlow<CollectionMarkResult?>(null)
	val markResult: LiveData<CollectionMarkResult?> = _markResult.asLiveData()

	/**
	 * Recalculates the unwatched/total counters, e.g. when the overview becomes visible again.
	 */
	fun refreshCounts() {
		showCollectionRepository.requestCountRefresh()
	}

	fun save(
		id: Int,
		name: String,
		searchQuery: String,
		excludeTerms: String,
		channels: String,
		detectEpisodes: Boolean,
		sortByEpisodes: Boolean,
		sortEpisodesAscending: Boolean,
	) {
		viewModelScope.launch {
			showCollectionRepository.save(
				id,
				name,
				searchQuery,
				excludeTerms,
				channels,
				detectEpisodes,
				sortByEpisodes,
				sortEpisodesAscending
			)
		}
	}

	fun delete(collection: ShowCollection) {
		viewModelScope.launch {
			showCollectionRepository.delete(collection)
		}
	}

	/**
	 * Marks all shows found for the collection as watched.
	 */
	fun markAllAsWatched(collection: ShowCollection) {
		viewModelScope.launch {
			_markResult.value = mark(CollectionMarkResult.Action.WATCHED) {
				showCollectionRepository.markAllAsWatched(collection)
			}
		}
	}

	/**
	 * Marks all shows found for the collection as unwatched.
	 */
	fun markAllAsUnwatched(collection: ShowCollection) {
		viewModelScope.launch {
			_markResult.value = mark(CollectionMarkResult.Action.UNWATCHED) {
				showCollectionRepository.markAllAsUnwatched(collection)
			}
		}
	}

	/**
	 * Runs the given action and makes sure that an unexpected error does not kill the app.
	 */
	private suspend fun mark(
		action: CollectionMarkResult.Action,
		block: suspend () -> Int,
	): CollectionMarkResult {
		return try {
			CollectionMarkResult(action, block())
		} catch (e: Exception) {
			Timber.e(e, "Could not mark all shows of the collection")
			CollectionMarkResult(action, 0, failed = true)
		}
	}
}
