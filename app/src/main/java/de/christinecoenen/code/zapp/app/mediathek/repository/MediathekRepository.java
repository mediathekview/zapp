package de.christinecoenen.code.zapp.app.mediathek.repository;

import androidx.paging.DataSource;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import de.christinecoenen.code.zapp.app.mediathek.api.MediathekService;
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest;
import de.christinecoenen.code.zapp.app.mediathek.model.DownloadStatus;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;
import de.christinecoenen.code.zapp.persistence.Database;
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor;
import io.reactivex.Completable;
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
	private final Database database;


	public MediathekRepository(Database database) {
		this.database = database;

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

	public DataSource.Factory<Integer, PersistedMediathekShow> getDownloads() {
		// TODO: filter for only queued, running or finished downloads
		// TODO: sort on date
		return database.mediathekShowDao().getAllDownloads();
	}

	public Flowable<PersistedMediathekShow> persistOrUpdateShow(MediathekShow show) {
		return Completable.fromAction(() -> database.mediathekShowDao().insertOrUpdate(show))
			.andThen(database.mediathekShowDao().getFromApiId(show.getApiId()))
			.subscribeOn(Schedulers.io());
	}

	public void updateShow(PersistedMediathekShow show) {
		database.mediathekShowDao()
			.update(show)
			.subscribeOn(Schedulers.io())
			.subscribe();
	}

	public void updateDownloadStatus(int downloadId, DownloadStatus downloadStatus) {
		database.mediathekShowDao()
			.updateDownloadStatus(downloadId, downloadStatus)
			.subscribeOn(Schedulers.io())
			.subscribe();
	}

	public void updateDownloadProgress(int downloadId, int progress) {
		database.mediathekShowDao()
			.updateDownloadProgress(downloadId, progress)
			.subscribeOn(Schedulers.io())
			.subscribe();
	}

	public void updateDownloadedVideoPath(int downloadId, String videoPath) {
		database.mediathekShowDao()
			.updateDownloadedVideoPath(downloadId, videoPath)
			.subscribeOn(Schedulers.io())
			.subscribe();
	}

	public Flowable<PersistedMediathekShow> getPersistedShow(int id) {
		return database.mediathekShowDao().getFromId(id).subscribeOn(Schedulers.io());
	}

	public Flowable<PersistedMediathekShow> getPersistedShowByApiId(String apiId) {
		return database.mediathekShowDao().getFromApiId(apiId).subscribeOn(Schedulers.io());
	}

	public Flowable<PersistedMediathekShow> getPersistedShowByDownloadId(int downloadId) {
		return database.mediathekShowDao().getFromDownloadId(downloadId).subscribeOn(Schedulers.io());
	}

	public Flowable<DownloadStatus> getDownloadStatus(String apiId) {
		return database
			.mediathekShowDao()
			.getDownloadStatus(apiId)
			.startWith(DownloadStatus.NONE)
			.subscribeOn(Schedulers.io());
	}

	public Flowable<Integer> getDownloadProgress(String apiId) {
		return database
			.mediathekShowDao()
			.getDownloadProgress(apiId)
			.subscribeOn(Schedulers.io());
	}

	public Single<Long> getPlaybackPosition(int showId) {
		return database.mediathekShowDao()
			.getPlaybackPosition(showId)
			.subscribeOn(Schedulers.io());
	}

	public void setPlaybackPosition(int showId, long positionMillis, long durationMillis) {
		database.mediathekShowDao()
			.setPlaybackPosition(showId, positionMillis, durationMillis, DateTime.now())
			.subscribeOn(Schedulers.io())
			.subscribe();
	}

	public Flowable<Float> getPlaybackPositionPercent(String apiId) {
		return database.mediathekShowDao()
			.getPlaybackPositionPercent(apiId)
			.startWith(0f)
			.distinct()
			.subscribeOn(Schedulers.io());
	}
}
