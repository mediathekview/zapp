package de.christinecoenen.code.zapp.app.player

import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.repositories.MediathekRepository

class PersistedPlaybackPositionRepository(
	private val mediathekRepository: MediathekRepository,
	private val settingsRepository: SettingsRepository,
) : IPlaybackPositionRepository {

	override suspend fun savePlaybackPosition(
		videoInfo: VideoInfo,
		positionMillis: Long,
		durationMillis: Long
	) {
		if (videoInfo.id == 0 || !videoInfo.hasDuration || durationMillis < 0) {
			return
		}

		mediathekRepository.setPlaybackPosition(
			videoInfo.id,
			clipPositionToWatchThreshold(positionMillis, durationMillis),
			durationMillis
		)
	}

	override suspend fun getPlaybackPosition(videoInfo: VideoInfo): Long {
		return if (videoInfo.id == 0) {
			0L
		} else {
			mediathekRepository.getPlaybackPosition(videoInfo.id)
		}
	}

	/**
	 * Returns the playback position to store. Once the user defined percentage of a show has
	 * been played, the show is stored as completely watched, so that it is treated as watched
	 * everywhere (faded out or hidden in the lists, not counted as unwatched).
	 *
	 * The stored position is used again as soon as the user rewinds below the threshold.
	 */
	private fun clipPositionToWatchThreshold(positionMillis: Long, durationMillis: Long): Long {
		val watchedPercent = settingsRepository.autoWatchedPercent

		if (watchedPercent <= 0 || durationMillis <= 0) {
			return positionMillis
		}

		return if (positionMillis * 100 >= durationMillis * watchedPercent) {
			durationMillis
		} else {
			positionMillis
		}
	}
}
