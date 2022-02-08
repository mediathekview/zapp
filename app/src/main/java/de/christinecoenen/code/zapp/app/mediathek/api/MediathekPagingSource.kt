package de.christinecoenen.code.zapp.app.mediathek.api

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class MediathekPagingSource(
	private val mediathekApi: IMediathekApiService,
	private val query: QueryRequest
) : PagingSource<Int, MediathekShow>() {

	override fun getRefreshKey(state: PagingState<Int, MediathekShow>): Int? {
		// Try to find the page key of the closest page to anchorPosition, from
		// either the prevKey or the nextKey, but you need to handle nullability
		// here:
		//  * prevKey == null -> anchorPage is the first page.
		//  * nextKey == null -> anchorPage is the last page.
		//  * both prevKey and nextKey null -> anchorPage is the initial page, so
		//    just return null.
		return state.anchorPosition?.let { anchorPosition ->
			val anchorPage = state.closestPageToPosition(anchorPosition)
			anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
		}
	}

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediathekShow> {
		return try {
			// Start refresh at page 1 if undefined.
			val nextPageNumber = params.key ?: 1
			query.size = params.loadSize
			query.offset = nextPageNumber.minus(1) * params.loadSize

			val response = mediathekApi.listShows(query)

			val showList = response.result?.results ?: throw Error(response.err)
			val nextKey = if (showList.isEmpty()) null else nextPageNumber.plus(1)

			LoadResult.Page(
				data = showList,
				prevKey = null, // Only paging forward.
				nextKey = nextKey
			)
		} catch (e: IOException) {
			// IOException for network failures.
			Timber.e(e)
			return LoadResult.Error(e)
		} catch (e: HttpException) {
			// HttpException for any non-2xx HTTP status codes.
			Timber.e(e)
			return LoadResult.Error(e)
		}

	}

}
