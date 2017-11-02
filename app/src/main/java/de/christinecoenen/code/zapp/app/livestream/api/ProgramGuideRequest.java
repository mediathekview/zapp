package de.christinecoenen.code.zapp.app.livestream.api;


import de.christinecoenen.code.zapp.app.livestream.api.model.Channel;
import de.christinecoenen.code.zapp.app.livestream.api.model.ShowResponse;
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class ProgramGuideRequest implements Callback<ShowResponse> {

	private final static ProgramInfoService service = new Retrofit.Builder()
		.baseUrl("https://zappbackend.herokuapp.com/v1/")
		.addConverterFactory(GsonConverterFactory.create())
		.build()
		.create(ProgramInfoService.class);

	private Listener listener;
	private Channel channelId;
	private Call<ShowResponse> showCall;

	public ProgramGuideRequest setListener(Listener listener) {
		this.listener = listener;
		return this;
	}

	@SuppressWarnings("WeakerAccess")
	public ProgramGuideRequest setChannelId(Channel channelId) {
		this.channelId = channelId;
		return this;
	}

	public ProgramGuideRequest setChannelId(String channelId) {
		Channel newChannel = null;
		try {
			newChannel = Channel.getById(channelId);
		} catch (IllegalArgumentException e) {
			Timber.w("%s is no valid channel id", channelId);
		}

		return setChannelId(newChannel);
	}

	public ProgramGuideRequest execute() {
		if (listener == null) {
			throw new RuntimeException("listener not set");
		}

		if (channelId == null) {
			Timber.w("no valid channel id set");
			listener.onRequestError();
			return this;
		}

		LiveShow cachedShow = Cache.getInstance().getShow(channelId);

		if (cachedShow != null) {
			listener.onRequestSuccess(cachedShow);
		} else {
			showCall = service.getShows(channelId.toString());
			showCall.enqueue(this);
		}

		return this;
	}

	public void cancel() {
		if (showCall != null) {
			showCall.cancel();
		}
	}

	@Override
	public void onResponse(Call<ShowResponse> call, Response<ShowResponse> response) {
		//noinspection ConstantConditions
		if (response.body() == null || !response.body().isSuccess()) {
			listener.onRequestError();
		} else {
			//noinspection ConstantConditions
			LiveShow liveShow = response.body().getShow().toLiveShow();
			Cache.getInstance().save(channelId, liveShow);
			listener.onRequestSuccess(liveShow);
		}
	}

	@Override
	public void onFailure(Call<ShowResponse> call, Throwable t) {
		if (!call.isCanceled()) {
			listener.onRequestError();
		}
	}

	public interface Listener {
		void onRequestError();

		void onRequestSuccess(LiveShow currentShow);
	}
}
