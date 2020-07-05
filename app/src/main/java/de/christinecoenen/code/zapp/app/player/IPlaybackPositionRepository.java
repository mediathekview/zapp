package de.christinecoenen.code.zapp.app.player;

import io.reactivex.Single;

public interface IPlaybackPositionRepository {

	void savePlaybackPosition(VideoInfo videoInfo, long millis);
	Single<Long> getPlaybackPosition(VideoInfo videoInfo);

}
