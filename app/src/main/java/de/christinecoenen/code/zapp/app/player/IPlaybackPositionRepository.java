package de.christinecoenen.code.zapp.app.player;

interface IPlaybackPositionRepository {

	void savePlaybackPosition(VideoInfo videoInfo, long millis);
	long getPlaybackPosition(VideoInfo videoInfo);

}
