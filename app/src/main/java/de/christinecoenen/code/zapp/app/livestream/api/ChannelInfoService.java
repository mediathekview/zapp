package de.christinecoenen.code.zapp.app.livestream.api;


import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo;
import de.christinecoenen.code.zapp.app.livestream.api.model.ShowResponse;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ChannelInfoService {

	@GET("shows/{channelName}")
	Single<ShowResponse> getShows(@Path("channelName") String channelName);

	@GET("channelInfo/{channelName}")
	Single<ChannelInfo> getChannelInfo(@Path("channelName") String channelName);

}
