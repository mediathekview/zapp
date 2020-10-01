package de.christinecoenen.code.zapp.app.player

import io.reactivex.Single

interface IPlaybackPositionRepository {

	fun savePlaybackPosition(videoInfo: VideoInfo, positionMillis: Long, durationMillis: Long)

	fun getPlaybackPosition(videoInfo: VideoInfo): Single<Long>

}
