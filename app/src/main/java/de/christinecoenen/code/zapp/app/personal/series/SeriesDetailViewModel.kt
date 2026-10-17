package de.christinecoenen.code.zapp.app.personal.series

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekPagingSource
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModel
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import de.christinecoenen.code.zapp.models.collections.isExcluded
import de.christinecoenen.code.zapp.models.shows.EpisodeComparator
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.ShowCollectionRepository
import de.christinecoenen.code.zapp.utils.paging.ListPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesDetailViewModel(
	private val showCollectionRepository: ShowCollectionRepository,
	mediathekApi: IMediathekApiService,
	private val collectionId: Int,
) : ViewModel() {

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
	}

	private val pagingConfig = PagingConfig(
		pageSize = ITEM_COUNT_PER_PAGE,
		enablePlaceholders = false
	)

	private val queryInfoResult = MutableStateFlow<QueryInfoResult?>(null)

	private val _collection = MutableStateFlow<ShowCollection?>(null)
	val collection: LiveData<ShowCollection?> = _collection.asLiveData()

	private val _markResult = MutableStateFlow<CollectionMarkResult?>(null)
	val markResult: LiveData<CollectionMarkResult?> = _markResult.asLiveData()

	val showList = showCollectionRepository
		.getFromId(collectionId)
		.filterNotNull()
		.distinctUntilChangedBy { collection ->
			ShowListSettings(
				searchQuery = collection.searchQuery,
				excludeTerms = collection.excludeTerms,
				detectEpisodes = collection.detectEpisodes,
				sortByEpisodes = collection.sortByEpisodes
			)
		}
		.flatMapLatest { collection ->
			val pagingDataFlow = if (collection.sortByEpisodes) {
				// the mediathek cannot sort by episode, so all shows are loaded and sorted here
				Pager(pagingConfig) {
					ListPagingSource {
						withContext(Dispatchers.Default) {
							showCollectionRepository
								.loadShows(collection)
								.sortedWith(EpisodeComparator)
						}
					}
				}.flow
			} else {
				Pager(pagingConfig) {
					MediathekPagingSource(
						mediathekApi,
						QueryRequest().apply {
							size = ITEM_COUNT_PER_PAGE
							setQueryString(collection.searchQuery)
						},
						queryInfoResult
					)
				}.flow
			}

			pagingDataFlow
				.map<PagingData<MediathekShow>, PagingData<MediathekShow>> { pagingData ->
					pagingData.filter { show -> !collection.isExcluded(show) }
				}
		}
		.map<PagingData<MediathekShow>, PagingData<UiModel>> { pagingData ->
			pagingData.map { show ->
				UiModel.MediathekShowModel(
					show,
					DateTime(show.timestamp.toLong() * DateUtils.SECOND_IN_MILLIS)
				)
			}
		}
		.cachedIn(viewModelScope)
		.asLiveData()

	/**
	 * Settings that require the list to be loaded again when they change.
	 */
	private data class ShowListSettings(
		val searchQuery: String,
		val excludeTerms: String,
		val detectEpisodes: Boolean,
		val sortByEpisodes: Boolean,
	)

	init {
		viewModelScope.launch {
			showCollectionRepository
				.getFromId(collectionId)
				.filterNotNull()
				.collect { _collection.value = it }
		}
	}

	fun markAllAsWatched() {
		viewModelScope.launch {
			_markResult.value = mark(CollectionMarkResult.Action.WATCHED) {
				val collection = showCollectionRepository.getFromId(collectionId).firstOrNull()
					?: return@mark 0

				showCollectionRepository.markAllAsWatched(collection)
			}
		}
	}

	fun markAllAsUnwatched() {
		viewModelScope.launch {
			_markResult.value = mark(CollectionMarkResult.Action.UNWATCHED) {
				val collection = showCollectionRepository.getFromId(collectionId).firstOrNull()
					?: return@mark 0

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
