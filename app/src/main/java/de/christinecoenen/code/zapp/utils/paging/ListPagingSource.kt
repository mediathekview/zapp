package de.christinecoenen.code.zapp.utils.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import timber.log.Timber

/**
 * Paging source for lists that are loaded as a whole, e.g. from a local database or from a
 * query that has to be sorted locally.
 *
 * The items are loaded lazily on the first page request and reused for all following pages.
 */
class ListPagingSource<T : Any>(
	private val loader: suspend () -> List<T>,
) : PagingSource<Int, T>() {

	private var items: List<T>? = null

	override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
		val allItems = try {
			items ?: loader().also { items = it }
		} catch (e: Exception) {
			Timber.e(e, "Could not load the items of the list")
			return LoadResult.Error(e)
		}

		val startIndex = params.key ?: 0
		val pageItems = allItems.drop(startIndex).take(params.loadSize)
		val nextIndex = startIndex + pageItems.size

		return LoadResult.Page(
			data = pageItems,
			prevKey = null,
			nextKey = if (nextIndex >= allItems.size) null else nextIndex
		)
	}
}
