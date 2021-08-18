package de.christinecoenen.code.zapp.app.player

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class Player(
	context: Context,
	private val playbackPositionRepository: IPlaybackPositionRepository
) {

	companion object {

		private const val LANGUAGE_GERMAN = "deu"

	}

	val exoPlayer: SimpleExoPlayer
	val mediaSession: MediaSessionCompat

	var currentVideoInfo: VideoInfo? = null
		private set

	val isBuffering: StateFlow<Boolean>
		get() = playerEventHandler.isBuffering

	val isIdle: Boolean
		get() = exoPlayer.playbackState == Player.STATE_IDLE

	val errorResourceId: Flow<Int?>
		get() = playerEventHandler
			.errorResourceId
			.combine(playerEventHandler.isIdle) { errorResourceId, isIdle ->
				if (isIdle) errorResourceId else -1
			}
			.distinctUntilChanged()


	private val playerEventHandler: PlayerEventHandler = PlayerEventHandler()
	private val settings: SettingsRepository = SettingsRepository(context)
	private val networkConnectionHelper: NetworkConnectionHelper = NetworkConnectionHelper(context)

	private val trackSelectorWrapper: TrackSelectorWrapper


	private val requiredStreamQualityBucket: StreamQualityBucket
		get() = if (networkConnectionHelper.isConnectedToUnmeteredNetwork || currentVideoInfo?.isOfflineVideo == true) {
			StreamQualityBucket.HIGHEST
		} else {
			settings.meteredNetworkStreamQuality
		}

	private var millis: Long
		get() = exoPlayer.currentPosition
		set(millis) {
			exoPlayer.seekTo(millis)
		}


	init {
		// quality selection
		val trackSelector = DefaultTrackSelector(context).apply {
			setParameters(
				this
					.buildUponParameters()
					.setPreferredAudioLanguage(LANGUAGE_GERMAN)
					.setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(context)
					.setSelectUndeterminedTextLanguage(true)
					.setDisabledTextTrackSelectionFlags(C.SELECTION_FLAG_DEFAULT)
			)
		}
		trackSelectorWrapper = TrackSelectorWrapper(trackSelector)

		// audio focus setup
		val audioAttributes = AudioAttributes.Builder()
			.setUsage(C.USAGE_MEDIA)
			.setContentType(C.CONTENT_TYPE_MOVIE)
			.build()

		exoPlayer = SimpleExoPlayer.Builder(context)
			.setTrackSelector(trackSelector)
			.setWakeMode(C.WAKE_MODE_NETWORK)
			.setAudioAttributes(audioAttributes, true)
			.build()

		// media session setup
		mediaSession = MediaSessionCompat(context, context.packageName)

		val mediaSessionConnector = MediaSessionConnector(mediaSession)
		mediaSessionConnector.setPlayer(exoPlayer)
		mediaSession.isActive = true

		// set listeners
		networkConnectionHelper.startListenForNetworkChanges(::setStreamQualityByNetworkType)
	}

	fun setView(videoView: StyledPlayerView) {
		videoView.player = exoPlayer
	}

	suspend fun load(videoInfo: VideoInfo) = withContext(Dispatchers.Main) {
		if (videoInfo == currentVideoInfo) {
			return@withContext
		}

		if (currentVideoInfo != null) {
			saveCurrentPlaybackPosition()
		}

		playerEventHandler.errorResourceId.emit(-1)
		currentVideoInfo = videoInfo

		val mediaItem = getMediaItem(videoInfo)
		exoPlayer.stop()
		exoPlayer.clearMediaItems()
		exoPlayer.addAnalyticsListener(playerEventHandler)
		exoPlayer.addMediaItem(mediaItem)

		exoPlayer.prepare()

		if (videoInfo.hasDuration) {
			millis = playbackPositionRepository.getPlaybackPosition(currentVideoInfo!!)
		}
	}

	suspend fun recreate() = withContext(Dispatchers.Main) {
		val oldVideoInfo = currentVideoInfo
		val oldPosition = millis

		currentVideoInfo = null
		load(oldVideoInfo!!)

		millis = oldPosition
	}

	fun pause() {
		exoPlayer.playWhenReady = false
	}

	fun resume() {
		exoPlayer.playWhenReady = true
	}

	fun rewind() {
		exoPlayer.seekBack()
	}

	fun fastForward() {
		exoPlayer.seekForward()
	}

	suspend fun destroy() = withContext(Dispatchers.Main) {
		saveCurrentPlaybackPosition()
		networkConnectionHelper.endListenForNetworkChanges()
		exoPlayer.removeAnalyticsListener(playerEventHandler)
		exoPlayer.release()
		mediaSession.release()
	}

	private suspend fun saveCurrentPlaybackPosition() {
		if (currentVideoInfo == null) {
			return
		}
		playbackPositionRepository.savePlaybackPosition(
			currentVideoInfo!!,
			millis,
			exoPlayer.duration
		)
	}

	private fun getMediaItem(videoInfo: VideoInfo?): MediaItem {
		val quality = requiredStreamQualityBucket
		val uri = videoInfo!!.getPlaybackUrlOrFilePath(quality)
		val mediaItemBuilder = MediaItem.Builder().setUri(uri)

		// add subtitles if present
		if (videoInfo.hasSubtitles) {
			val subtitle = MediaItem.Subtitle(
				Uri.parse(videoInfo.subtitleUrl),
				videoInfo.subtitleUrl!!.toSubtitleMimeType(),
				LANGUAGE_GERMAN,
				C.SELECTION_FLAG_AUTOSELECT
			)

			mediaItemBuilder.setSubtitles(listOf(subtitle))
		}

		return mediaItemBuilder.build()
	}

	private fun setStreamQuality(streamQuality: StreamQualityBucket) {
		when (streamQuality) {
			StreamQualityBucket.DISABLED -> {
				exoPlayer.stop()
				exoPlayer.removeAnalyticsListener(playerEventHandler)
				playerEventHandler.errorResourceId.tryEmit(R.string.error_stream_not_in_unmetered_network)
			}
			else -> trackSelectorWrapper.setStreamQuality(streamQuality)
		}
	}

	private fun setStreamQualityByNetworkType() {
		setStreamQuality(requiredStreamQualityBucket)
	}

	private fun String.toSubtitleMimeType(): String {
		return when {
			endsWith("vtt", true) -> MimeTypes.TEXT_VTT
			endsWith("xml", true) || endsWith("ttml", true) -> MimeTypes.APPLICATION_TTML
			else -> MimeTypes.APPLICATION_TTML
		}
	}
}
