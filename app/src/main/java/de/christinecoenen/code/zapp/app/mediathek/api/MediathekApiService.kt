package de.christinecoenen.code.zapp.app.mediathek.api

import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.MediathekAnswer
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MediathekApiService {

	@Headers("Content-Type: text/plain")
	@POST("query")
	fun listShows(@Body queryRequest: QueryRequest): Single<MediathekAnswer>

}
