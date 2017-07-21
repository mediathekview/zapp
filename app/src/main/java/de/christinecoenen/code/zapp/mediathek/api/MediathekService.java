package de.christinecoenen.code.zapp.mediathek.api;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MediathekService {

	@Headers("Content-Type: text/plain")
	@POST("query")
	Call<MediathekAnswer> listShows(@Body QueryRequest queryRequest);
}
