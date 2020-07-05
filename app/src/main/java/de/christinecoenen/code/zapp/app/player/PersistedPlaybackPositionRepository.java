package de.christinecoenen.code.zapp.app.player;

import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository;
import io.reactivex.Single;

public class PersistedPlaybackPositionRepository implements IPlaybackPositionRepository {

	private final MediathekRepository mediathekRepository;

	public PersistedPlaybackPositionRepository(MediathekRepository mediathekRepository) {
		this.mediathekRepository = mediathekRepository;
	}

	@Override
	public void savePlaybackPosition(VideoInfo videoInfo, long millis) {
		if (videoInfo.getId() == 0) {
			return;
		}

		mediathekRepository.setPlaybackPosition(videoInfo.getId(), millis);
	}

	@Override
	public Single<Long> getPlaybackPosition(VideoInfo videoInfo) {
		if (videoInfo.getId() == 0) {
			return Single.just(0L);
		}

		return mediathekRepository.getPlaybackProsition(videoInfo.getId());
	}
}
