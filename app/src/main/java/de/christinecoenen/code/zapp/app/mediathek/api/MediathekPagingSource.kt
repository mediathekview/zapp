package de.christinecoenen.code.zapp.app.mediathek.api

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class MediathekPagingSource(
	private val mediathekApi: IMediathekApiService,
	private val query: QueryRequest,
	private val queryInfoResultPublisher: MutableStateFlow<QueryInfoResult?>
) : PagingSource<Int, MediathekShow>() {

	override fun getRefreshKey(state: PagingState<Int, MediathekShow>): Int {
		return ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2)
			.coerceAtLeast(0)
	}

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediathekShow> {

		queryInfoResultPublisher.emit(null)

		return try {
			query.size = params.loadSize
			query.offset = params.key ?: 0

			val response = mediathekApi.listShows(query)

			val showList = response.result?.results ?: throw Error(response.err)
			val nextOffset = if (showList.size < query.size) null else query.offset + query.size

			queryInfoResultPublisher.emit(response.result.queryInfo)
			delay(1000)

			LoadResult.Page(
				data = showList,
				prevKey = null, // Only paging forward.
				nextKey = nextOffset
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
