package de.christinecoenen.code.zapp.app.player

import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository
import io.reactivex.Single

class PersistedPlaybackPositionRepository(private val mediathekRepository: MediathekRepository) : IPlaybackPositionRepository {

	override fun savePlaybackPosition(videoInfo: VideoInfo, millis: Long) {
		if (videoInfo.id == 0) {
			return
		}

		mediathekRepository.setPlaybackPosition(videoInfo.id, millis)
	}

	override fun getPlaybackPosition(videoInfo: VideoInfo): Single<Long> {
		return if (videoInfo.id == 0) {
			Single.just(0L)
		} else {
			mediathekRepository.getPlaybackProsition(videoInfo.id)
		}
	}

}
