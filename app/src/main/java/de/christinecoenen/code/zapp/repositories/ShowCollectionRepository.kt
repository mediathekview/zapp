package de.christinecoenen.code.zapp.repositories

import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import de.christinecoenen.code.zapp.models.collections.isExcluded
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.persistence.Database
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import timber.log.Timber

/**
 * Stores user defined show collections and handles the mediathek searches
 * that belong to them.
 */
class ShowCollectionRepository(
	private val database: Database,
	private val mediathekApi: IMediathekApiService,
	private val mediathekRepository: MediathekRepository,
	private val settingsRepository: SettingsRepository,
) {

	companion object {
		/**
		 * Number of shows requested per mediathek page.
		 */
		private const val PAGE_SIZE = 100

		/**
		 * Number of shows requested per page when a collection is loaded as a whole, so that it
		 * can be sorted locally.
		 */
		private const val LARGE_PAGE_SIZE = 1000

		/**
		 * Time after which the total number of found shows is fetched again.
		 */
		private const val COUNT_MAX_AGE_HOURS = 6
	}

	private val refreshTrigger = MutableStateFlow(0)
	private val refreshScope = CoroutineScope(
		SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
			Timber.e(throwable, "Error while refreshing collection counters")
		}
	)
	private val refreshMutex = Mutex()

	/**
	 * Result of a search for all shows of a collection.
	 *
	 * @param visibleApiIds Api ids of the shows that belong to the collection. Excluded shows
	 *   are not included.
	 * @param changedCount Number of shows that have been changed by an action.
	 * @param isComplete Whether the whole mediathek result set has been processed.
	 * @param reachedLimit Whether the run was stopped by the configured limit of shows that
	 *   should be processed. The found shows are still used in that case.
	 */
	private data class Enumeration(
		val visibleApiIds: List<String>,
		val changedCount: Int,
		val isComplete: Boolean,
		val reachedLimit: Boolean,
	) {
		/**
		 * Whether the shows of the collection can be derived from this enumeration. False if a
		 * network error stopped the run - in that case the previous state is kept.
		 */
		val isUsable: Boolean
			get() = isComplete || reachedLimit
	}

	/**
	 * A collection that contains shows which have not been seen before.
	 */
	data class CollectionUpdate(val collection: ShowCollection, val newShowCount: Int)

	/**
	 * Observes all collections and keeps their unwatched/total counters up to date.
	 */
	fun getAllWithCounts(): Flow<List<ShowCollection>> {
		return combine(getAll(), refreshTrigger) { collections, _ -> collections }
			.onEach { collections -> requestCountRefreshFor(collections) }
	}

	/**
	 * Observes the most recent collections and keeps their counters up to date.
	 */
	fun getRecentWithCounts(limit: Int): Flow<List<ShowCollection>> {
		return combine(getRecent(limit), refreshTrigger) { collections, _ -> collections }
			.onEach { collections -> requestCountRefreshFor(collections) }
	}

	/**
	 * Refreshes the counters in the background, so that observers of the collection flow
	 * do not have to wait for network requests.
	 */
	private fun requestCountRefreshFor(collections: List<ShowCollection>) {
		if (collections.isEmpty()) {
			return
		}

		refreshScope.launch {
			try {
				refreshMutex.withLock {
					collections.forEach { refreshCount(it) }
				}
			} catch (e: Exception) {
				Timber.e(e, "Could not refresh collection counters")
			}
		}
	}

	/**
	 * Requests a refresh of all cached counters, e.g. when an overview becomes visible again.
	 */
	fun requestCountRefresh() {
		refreshTrigger.value++
	}

	fun getAll(): Flow<List<ShowCollection>> {
		return database
			.showCollectionDao()
			.getAll()
			.distinctUntilChanged()
			.flowOn(Dispatchers.IO)
	}

	fun getRecent(limit: Int): Flow<List<ShowCollection>> {
		return database
			.showCollectionDao()
			.getRecent(limit)
			.distinctUntilChanged()
			.flowOn(Dispatchers.IO)
	}

	fun getFromId(id: Int): Flow<ShowCollection?> {
		return database
			.showCollectionDao()
			.getFromId(id)
			.distinctUntilChanged()
			.flowOn(Dispatchers.IO)
	}

	suspend fun save(
		id: Int,
		name: String,
		searchQuery: String,
		excludeTerms: String,
		detectEpisodes: Boolean,
		sortByEpisodes: Boolean,
		sortEpisodesAscending: Boolean,
	) = withContext(Dispatchers.IO) {
		val dao = database.showCollectionDao()
		val trimmedName = name.trim()
		val trimmedQuery = searchQuery.trim()
		val trimmedExcludeTerms = excludeTerms.trim()

		if (id == 0) {
			dao.insert(
				ShowCollection(
					name = trimmedName,
					searchQuery = trimmedQuery,
					excludeTerms = trimmedExcludeTerms,
					detectEpisodes = detectEpisodes,
					sortByEpisodes = sortByEpisodes,
					sortEpisodesAscending = sortEpisodesAscending
				)
			)
		} else {
			val existing = dao.getFromIdSync(id) ?: return@withContext
			val queryChanged = existing.searchQuery != trimmedQuery ||
				existing.excludeTerms != trimmedExcludeTerms

			dao.update(
				existing.copy(
					name = trimmedName,
					searchQuery = trimmedQuery,
					excludeTerms = trimmedExcludeTerms,
					detectEpisodes = detectEpisodes,
					sortByEpisodes = sortByEpisodes,
					sortEpisodesAscending = sortEpisodesAscending,
					totalCount = if (queryChanged) 0 else existing.totalCount,
					unwatchedCount = if (queryChanged) 0 else existing.unwatchedCount,
					countUpdatedAt = if (queryChanged) null else existing.countUpdatedAt,
					lastKnownShowTimestamp =
						if (queryChanged) 0 else existing.lastKnownShowTimestamp
				)
			)
		}
	}

	suspend fun delete(collection: ShowCollection) = withContext(Dispatchers.IO) {
		database.showCollectionDao().delete(collection)
	}

	/**
	 * Loads all shows of the collection. Excluded shows are removed. At most
	 * [SettingsRepository.maxProcessedShows] shows are loaded.
	 *
	 * This is used when the shows have to be sorted locally instead of by the mediathek.
	 */
	suspend fun loadShows(collection: ShowCollection): List<MediathekShow> =
		withContext(Dispatchers.IO) {
			val maxProcessedShows = settingsRepository.maxProcessedShows
			val handledApiIds = mutableSetOf<String>()
			val shows = mutableListOf<MediathekShow>()
			var offset = 0

			while (offset < maxProcessedShows) {
				val page = try {
					searchMediathek(collection.searchQuery, LARGE_PAGE_SIZE, offset)
				} catch (e: Exception) {
					Timber.e(e, "Could not load shows of collection ${collection.id}")
					break
				}

				if (page.isEmpty()) {
					break
				}

				for (show in page) {
					if (collection.isExcluded(show) || !handledApiIds.add(show.apiId)) {
						continue
					}

					shows += show
				}

				if (page.size < LARGE_PAGE_SIZE) {
					break
				}

				offset += page.size
			}

			shows
		}

	/**
	 * Searches the mediatheks for the collection query and marks all found
	 * shows as watched. Shows that are already marked as watched are skipped.
	 *
	 * The mediathek search is paginated, so *all* results are processed, not only
	 * the first page.
	 *
	 * @return The number of shows that have been marked as watched.
	 */
	suspend fun markAllAsWatched(collection: ShowCollection): Int = withContext(Dispatchers.IO) {
		val enumeration = enumerateVisibleShows(collection) { show ->
			mediathekRepository.insertOrUpdateShow(show)
			mediathekRepository.markAsPlayedIfUnwatched(show.apiId) > 0
		}

		updateCountsAfterAction(collection, enumeration)

		enumeration.changedCount
	}

	/**
	 * Searches the mediatheks for the collection query and marks all found
	 * shows as unwatched. Shows that are not watched are skipped.
	 *
	 * The mediathek search is paginated, so *all* results are processed, not only
	 * the first page.
	 *
	 * @return The number of shows that have been marked as unwatched.
	 */
	suspend fun markAllAsUnwatched(collection: ShowCollection): Int = withContext(Dispatchers.IO) {
		val enumeration = enumerateVisibleShows(collection) { show ->
			// shows that are not stored locally cannot be watched
			mediathekRepository.resetPlaybackPositionIfPlayed(show.apiId) > 0
		}

		updateCountsAfterAction(collection, enumeration)

		enumeration.changedCount
	}

	/**
	 * Checks all collections for shows that are newer than the last known one and updates
	 * the stored state. The first check of a collection only stores the current state and
	 * does not report anything.
	 *
	 * @return All collections that contain new shows.
	 */
	suspend fun findCollectionsWithNewShows(): List<CollectionUpdate> = withContext(Dispatchers.IO) {
		val updates = mutableListOf<CollectionUpdate>()

		for (collection in database.showCollectionDao().getAllSync()) {
			try {
				val newShowCount = findNewShowCount(collection)
				if (newShowCount > 0) {
					updates += CollectionUpdate(collection, newShowCount)
				}
			} catch (e: Exception) {
				Timber.e(e, "Could not check collection ${collection.id} for new shows")
			}
		}

		updates
	}

	/**
	 * Number of shows of the collection that are newer than the last known one.
	 *
	 * The mediathek results are sorted by timestamp (newest first), so paging can stop at
	 * the first already known show.
	 */
	private suspend fun findNewShowCount(collection: ShowCollection): Int {
		if (collection.lastKnownShowTimestamp <= 0) {
			// first check: only remember the current state, the user just created the collection
			val newestTimestamp = searchMediathek(collection.searchQuery, 1)
				.firstOrNull()
				?.timestamp
				?: return 0

			if (newestTimestamp > 0) {
				database.showCollectionDao()
					.updateLastKnownShowTimestamp(collection.id, newestTimestamp)
			}

			return 0
		}

		val handledApiIds = mutableSetOf<String>()
		val maxProcessedShows = settingsRepository.maxProcessedShows
		var newShowCount = 0
		var newestTimestamp = 0
		var offset = 0
		var reachedKnownShow = false

		while (!reachedKnownShow && offset < maxProcessedShows) {
			val page = searchMediathek(collection.searchQuery, PAGE_SIZE, offset)

			if (page.isEmpty()) {
				break
			}

			for (show in page) {
				if (!handledApiIds.add(show.apiId)) {
					continue
				}

				if (show.timestamp > newestTimestamp) {
					newestTimestamp = show.timestamp
				}

				if (show.timestamp <= collection.lastKnownShowTimestamp) {
					reachedKnownShow = true
					break
				}

				if (!collection.isExcluded(show)) {
					newShowCount++
				}
			}

			if (page.size < PAGE_SIZE) {
				break
			}

			offset += page.size
		}

		if (newestTimestamp > collection.lastKnownShowTimestamp) {
			database.showCollectionDao()
				.updateLastKnownShowTimestamp(collection.id, newestTimestamp)
		}

		return newShowCount
	}

	/**
	 * Pages through all mediathek results of the given collection and calls [update] for
	 * every show. Excluded shows and duplicates are skipped.
	 *
	 * @return The api ids of all shows that belong to the collection and the result of the
	 *   update calls.
	 */
	private suspend fun enumerateVisibleShows(
		collection: ShowCollection,
		update: suspend (MediathekShow) -> Boolean = { false },
	): Enumeration {
		val handledApiIds = mutableSetOf<String>()
		val visibleApiIds = mutableListOf<String>()
		val maxProcessedShows = settingsRepository.maxProcessedShows
		var changedCount = 0
		var offset = 0
		var isComplete = false
		var hasError = false

		while (offset < maxProcessedShows) {
			val page = try {
				searchMediathek(
					searchQuery = collection.searchQuery,
					size = PAGE_SIZE,
					offset = offset
				)
			} catch (e: Exception) {
				Timber.e(e, "Could not load shows of collection ${collection.id} (offset $offset)")
				hasError = true
				break
			}

			if (page.isEmpty()) {
				isComplete = true
				break
			}

			for (show in page) {
				if (collection.isExcluded(show) || !handledApiIds.add(show.apiId)) {
					continue
				}

				visibleApiIds += show.apiId

				try {
					if (update(show)) {
						changedCount++
					}
				} catch (e: Exception) {
					Timber.e(e, "Could not update show ${show.apiId}")
				}
			}

			if (page.size < PAGE_SIZE) {
				// last page reached
				isComplete = true
				break
			}

			offset += page.size
		}

		return Enumeration(
			visibleApiIds = visibleApiIds,
			changedCount = changedCount,
			isComplete = isComplete,
			reachedLimit = !isComplete && !hasError
		)
	}

	/**
	 * Stores which shows belong to the collection and updates its counters. This is called
	 * after "mark all as watched/unwatched", where all shows are known anyway.
	 */
	private suspend fun updateCountsAfterAction(
		collection: ShowCollection,
		enumeration: Enumeration,
	) {
		if (!enumeration.isUsable) {
			// network problem - keep the previous state
			return
		}

		storeEntries(collection.id, enumeration.visibleApiIds)
		updateCounters(collection, markAsRefreshed = true)
	}

	/**
	 * Recalculates the counters of a collection. The shows that belong to the collection are
	 * only searched again when the stored state is stale - otherwise the counters are read
	 * from the stored shows, so watching a show updates them immediately.
	 */
	private suspend fun refreshCount(collection: ShowCollection) {
		val storedUpdatedAt = collection.countUpdatedAt
		val isStale = storedUpdatedAt == null ||
			storedUpdatedAt.plusHours(COUNT_MAX_AGE_HOURS).isBeforeNow()

		if (isStale) {
			val enumeration = enumerateVisibleShows(collection)

			if (!enumeration.isUsable) {
				// network problem - keep the previous state and retry later
				return
			}

			storeEntries(collection.id, enumeration.visibleApiIds)
		}

		updateCounters(collection, markAsRefreshed = isStale)
	}

	/**
	 * Stores which shows belong to the collection.
	 */
	private suspend fun storeEntries(collectionId: Int, apiIds: Collection<String>) {
		database.showCollectionEntryDao().replaceForCollection(collectionId, apiIds)
	}

	/**
	 * Reads the counters of the collection from the stored shows and saves them if they
	 * changed.
	 *
	 * @param markAsRefreshed Whether the mediathek has just been searched for the collection
	 *   and the stored state is up to date again.
	 */
	private suspend fun updateCounters(collection: ShowCollection, markAsRefreshed: Boolean) {
		val entryDao = database.showCollectionEntryDao()
		val totalCount = entryDao.countForCollection(collection.id)
		val unwatchedCount = entryDao.countUnwatchedForCollection(collection.id)

		if (!markAsRefreshed &&
			totalCount == collection.totalCount &&
			unwatchedCount == collection.unwatchedCount
		) {
			return
		}

		val dao = database.showCollectionDao()
		if (markAsRefreshed) {
			dao.updateCountersAndRefreshDate(
				collection.id,
				totalCount,
				unwatchedCount,
				DateTime.now()
			)
		} else {
			dao.updateCounters(collection.id, totalCount, unwatchedCount)
		}
	}

	private suspend fun searchMediathek(
		searchQuery: String,
		size: Int,
		offset: Int = 0,
	) = mediathekApi
		.listShows(
			QueryRequest().apply {
				this.size = size
				this.offset = offset
				setQueryString(searchQuery)
			}
		)
		.result
		?.results
		?: emptyList()
}
