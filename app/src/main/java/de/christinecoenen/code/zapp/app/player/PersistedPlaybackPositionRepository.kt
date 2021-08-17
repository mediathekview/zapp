package de.christinecoenen.code.zapp.app.player

import de.christinecoenen.code.zapp.repositories.MediathekRepository

class PersistedPlaybackPositionRepository(
	private val mediathekRepository: MediathekRepository
) : IPlaybackPositionRepository {

	override suspend fun savePlaybackPosition(
		videoInfo: VideoInfo,
		positionMillis: Long,
		durationMillis: Long
	) {
		if (videoInfo.id == 0) {
			return
		}

		mediathekRepository.setPlaybackPosition(videoInfo.id, positionMillis, durationMillis)
	}

	override suspend fun getPlaybackPosition(videoInfo: VideoInfo): Long {
		return if (videoInfo.id == 0) {
			0L
		} else {
			mediathekRepository.getPlaybackPosition(videoInfo.id)
		}
	}

}
