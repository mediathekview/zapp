package de.christinecoenen.code.zapp.app.mediathek.api

import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.MediathekAnswer
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IMediathekApiService {

	@Headers("Content-Type: application/json")
	@POST("query")
	suspend fun listShows(@Body queryRequest: QueryRequest): MediathekAnswer

}
