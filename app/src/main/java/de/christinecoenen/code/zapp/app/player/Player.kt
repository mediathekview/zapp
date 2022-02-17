package de.christinecoenen.code.zapp.app.player


import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.player.VideoInfoArtworkExtensions.getArtworkByteArray
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException


class Player(
	private val context: Context,
	private val playbackPositionRepository: IPlaybackPositionRepository
) {

	companion object {

		private const val LANGUAGE_GERMAN = "deu"

	}

	val exoPlayer: ExoPlayer
	val mediaSession: MediaSessionCompat

	var currentVideoInfo: VideoInfo? = null
		private set

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

		exoPlayer = ExoPlayer.Builder(context)
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
		networkConnectionHelper.startListenForNetworkChanges(::loadStreamQualityByNetworkType)
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

		loadStreamQualityByNetworkType()

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

		val mediaMetadataBuilder = MediaMetadata.Builder()
			.setTitle(videoInfo.title)
			.setArtist(videoInfo.subtitle)

		if (videoInfo.hasArtwork) {
			try {
				val artworkData = videoInfo.getArtworkByteArray(context)
				mediaMetadataBuilder
					.setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
			} catch (e: IOException) {
				// this is okay
				Timber.w(e)
			}
		}

		val mediaItemBuilder = MediaItem.Builder()
			.setUri(uri)
			.setMediaMetadata(mediaMetadataBuilder.build())

		// add subtitles if present
		if (videoInfo.hasSubtitles) {
			val subtitle = MediaItem.SubtitleConfiguration
				.Builder(Uri.parse(videoInfo.subtitleUrl))
				.setMimeType(videoInfo.subtitleUrl!!.toSubtitleMimeType())
				.setLanguage(LANGUAGE_GERMAN)
				.setSelectionFlags(C.SELECTION_FLAG_AUTOSELECT)
				.build()

			mediaItemBuilder.setSubtitleConfigurations(listOf(subtitle))
		}

		return mediaItemBuilder.build()
	}

	private fun loadStreamQuality(streamQuality: StreamQualityBucket) {
		when (streamQuality) {
			StreamQualityBucket.DISABLED -> {
				// needed to bubble error message correctly
				exoPlayer.addAnalyticsListener(playerEventHandler)
				exoPlayer.prepare()

				// TODO: save current playback position to resume playback on same position when wifi is available

				// stop playback and emit error
				exoPlayer.stop()
				exoPlayer.clearMediaItems()
				exoPlayer.removeAnalyticsListener(playerEventHandler)
				playerEventHandler.errorResourceId.tryEmit(R.string.error_stream_not_in_unmetered_network)
			}
			else -> {
				if (currentVideoInfo == null) {
					return
				}

				// adjust quality for adaptive streams
				trackSelectorWrapper.setStreamQuality(streamQuality)

				// TODO: wrong playback position
				// (Re)load item with correct quality url.
				// This is not strictly needed for adaptive streams and downloaded videos,
				// but we have no way to differentiate between stream types here.
				val mediaItem = getMediaItem(currentVideoInfo)
				exoPlayer.stop()
				exoPlayer.clearMediaItems()
				exoPlayer.addAnalyticsListener(playerEventHandler)
				exoPlayer.addMediaItem(mediaItem)
				exoPlayer.prepare()
			}
		}
	}

	private fun loadStreamQualityByNetworkType() {
		Timber.w(
			"setStreamQualityByNetworkType - isMetered: %s, quality: %s",
			!networkConnectionHelper.isConnectedToUnmeteredNetwork,
			requiredStreamQualityBucket
		)
		loadStreamQuality(requiredStreamQualityBucket)
	}

	private fun String.toSubtitleMimeType(): String {
		return when {
			endsWith("vtt", true) -> MimeTypes.TEXT_VTT
			endsWith("xml", true) || endsWith("ttml", true) -> MimeTypes.APPLICATION_TTML
			else -> MimeTypes.APPLICATION_TTML
		}
	}
}
