package de.christinecoenen.code.zapp.app.player

interface IPlaybackPositionRepository {

	suspend fun savePlaybackPosition(
		videoInfo: VideoInfo,
		positionMillis: Long,
		durationMillis: Long
	)

	suspend fun getPlaybackPosition(videoInfo: VideoInfo): Long

}
