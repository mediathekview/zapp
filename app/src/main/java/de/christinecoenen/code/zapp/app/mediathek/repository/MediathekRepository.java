package de.christinecoenen.code.zapp.app.mediathek.repository;

import java.util.Collections;
import java.util.List;

import de.christinecoenen.code.zapp.app.mediathek.api.MediathekService;
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.repository.persistence.MediathekDatabase;
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;

public class MediathekRepository {


	private final MediathekService service;
	private final MediathekDatabase database;


	public MediathekRepository(MediathekDatabase mediathekDatabase) {
		this.database = mediathekDatabase;

		// workaround to avoid SSLHandshakeException on Android 7 devices
		// see: https://stackoverflow.com/questions/39133437/sslhandshakeexception-handshake-failed-on-android-n-7-0
		ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
			.tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
			.build();

		OkHttpClient client = new OkHttpClient.Builder()
			.connectionSpecs(Collections.singletonList(spec))
			.addInterceptor(new UserAgentInterceptor())
			.build();

		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("https://mediathekviewweb.de/api/")
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build();

		service = retrofit.create(MediathekService.class);
	}

	public Single<List<MediathekShow>> listShows(@Body QueryRequest queryRequest) {
		return service.listShows(queryRequest)
			.subscribeOn(Schedulers.io())
			.map(mediathekAnswer -> {
				if (mediathekAnswer == null || mediathekAnswer.result == null || mediathekAnswer.err != null) {
					throw new RuntimeException("Empty result");
				}
				return mediathekAnswer.result.results;
			});
	}

	public void persistShow(MediathekShow show) {
		PersistedMediathekShow persistedShow = new PersistedMediathekShow();
		persistedShow.setMediathekShow(show);
		database.mediathekShowDao()
			.insert(persistedShow)
			.subscribeOn(Schedulers.io())
			.subscribe();
	}

	public Flowable<PersistedMediathekShow> getPersistedShow(String apiId) {
		return database.mediathekShowDao().getFromApiId(apiId).subscribeOn(Schedulers.io());
	}
}
