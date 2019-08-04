package de.christinecoenen.code.zapp.app.livestream.repository;

import java.util.Map;

import de.christinecoenen.code.zapp.app.livestream.api.ChannelInfoService;
import de.christinecoenen.code.zapp.app.livestream.api.model.Channel;
import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo;
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow;
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class ChannelInfoRepository {

	private static ChannelInfoRepository instance;

	public static ChannelInfoRepository getInstance() {
		if (instance == null) {
			instance = new ChannelInfoRepository();
		}
		return instance;
	}

	private final Cache cache;
	private final ChannelInfoService service;

	private ChannelInfoRepository() {
		OkHttpClient client = new OkHttpClient.Builder()
			.addInterceptor(new UserAgentInterceptor())
			.build();

		service = new Retrofit.Builder()
			.baseUrl("https://zappbackend.herokuapp.com/v1/")
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build()
			.create(ChannelInfoService.class);

		cache = new Cache();
	}

	public Single<LiveShow> getShows(String channelId) {
		Channel newChannel = null;
		try {
			newChannel = Channel.getById(channelId);
		} catch (IllegalArgumentException e) {
			Timber.w("%s is no valid channel id", channelId);
		}

		return getShows(newChannel);
	}

	public Single<Map<String, ChannelInfo>> getChannelInfoList() {
		return service.getChannelInfoList()
			.subscribeOn(Schedulers.io());
	}

	private Single<LiveShow> getShows(Channel channel) {
		LiveShow cachedShow = cache.getShow(channel);

		if (cachedShow != null) {
			return Single.just(cachedShow);
		}

		return service.getShows(channel.toString())
			.subscribeOn(Schedulers.io())
			.map(showResponse -> {
				if (!showResponse.isSuccess()) {
					throw new RuntimeException("Show response was empty");
				} else {
					LiveShow liveShow = showResponse.getShow().toLiveShow();
					cache.save(channel, liveShow);
					return liveShow;
				}
			});
	}
}
