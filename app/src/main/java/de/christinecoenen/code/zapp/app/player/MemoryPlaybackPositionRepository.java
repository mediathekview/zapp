package de.christinecoenen.code.zapp.app.player;

import java.util.HashMap;
import java.util.Map;

public class MemoryPlaybackPositionRepository implements IPlaybackPositionRepository {

	private final Map<String, Long> playbackPositions = new HashMap<>();

	@Override
	public void savePlaybackPosition(VideoInfo videoInfo, long millis) {
		playbackPositions.put(videoInfo.getUrl(), millis);
	}

	@Override
	public long getPlaybackPosition(VideoInfo videoInfo) {
		String url = videoInfo.getUrl();
		//noinspection ConstantConditions
		return playbackPositions.containsKey(url) ? playbackPositions.get(url) : 0;
	}
}
