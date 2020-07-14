package de.christinecoenen.code.zapp.app.player;

import androidx.annotation.NonNull;

import io.reactivex.Single;

public interface IPlaybackPositionRepository {

	void savePlaybackPosition(@NonNull VideoInfo videoInfo, long positionMillis, long durationMillis);

	Single<Long> getPlaybackPosition(@NonNull VideoInfo videoInfo);

}
