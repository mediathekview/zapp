package de.christinecoenen.code.zapp.app.player

import de.christinecoenen.code.zapp.repositories.MediathekRepository
import io.reactivex.Single

class PersistedPlaybackPositionRepository(private val mediathekRepository: MediathekRepository) : IPlaybackPositionRepository {

	override fun savePlaybackPosition(videoInfo: VideoInfo, positionMillis: Long, durationMillis: Long) {
		if (videoInfo.id == 0) {
			return
		}

		mediathekRepository.setPlaybackPosition(videoInfo.id, positionMillis, durationMillis)
	}

	override fun getPlaybackPosition(videoInfo: VideoInfo): Single<Long> {
		return if (videoInfo.id == 0) {
			Single.just(0L)
		} else {
			mediathekRepository.getPlaybackPosition(videoInfo.id)
		}
	}

}
