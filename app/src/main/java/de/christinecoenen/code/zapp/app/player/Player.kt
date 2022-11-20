package de.christinecoenen.code.zapp.app.player


import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.player.VideoInfoArtworkExtensions.getArtworkByteArray
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper
import de.christinecoenen.code.zapp.utils.video.ScreenDimmingHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.IOException


class Player(
	private val context: Context,
	private val applicationScope: CoroutineScope,
	httpClient: OkHttpClient,
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
	private val screenDimmingHandler = ScreenDimmingHandler()
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
		val trackSelector = DefaultTrackSelector(context).also {
			it.setParameters(
				it
					.buildUponParameters()
					.setPreferredAudioLanguage(LANGUAGE_GERMAN)
					.setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(context)
					.setSelectUndeterminedTextLanguage(true)
					.setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
			)
		}
		trackSelectorWrapper = TrackSelectorWrapper(trackSelector)

		// audio focus setup
		val audioAttributes = AudioAttributes.Builder()
			.setUsage(C.USAGE_MEDIA)
			.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
			.build()

		// use okhttp as network stack
		val dataSourceFactory = DefaultDataSource.Factory(
			context,
			OkHttpDataSource.Factory(httpClient)
		)
		exoPlayer = ExoPlayer.Builder(context)
			.setTrackSelector(trackSelector)
			.setWakeMode(C.WAKE_MODE_NETWORK)
			.setAudioAttributes(audioAttributes, true)
			.setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
			.build()

		// media session setup
		mediaSession = MediaSessionCompat(context, context.packageName)

		val mediaSessionConnector = MediaSessionConnector(mediaSession)
		mediaSessionConnector.setPlayer(exoPlayer)
		mediaSession.isActive = true

		// set listeners
		networkConnectionHelper.startListenForNetworkChanges(::loadStreamQualityByNetworkType)
		exoPlayer.addListener(screenDimmingHandler)
	}

	fun setView(videoView: StyledPlayerView) {
		videoView.player = exoPlayer
		screenDimmingHandler.setScreenToKeepOn(videoView)
	}

	fun setView(parentView: View) {
		screenDimmingHandler.setScreenToKeepOn(parentView)
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

	private suspend fun loadStreamQuality(streamQuality: StreamQualityBucket) {
		when (streamQuality) {
			StreamQualityBucket.DISABLED -> {
				// needed to bubble error message correctly
				exoPlayer.addAnalyticsListener(playerEventHandler)
				exoPlayer.prepare()

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

				// Adjust quality for adaptive streams
				trackSelectorWrapper.setStreamQuality(streamQuality)

				// Which item should be use for our current quality?
				val mediaItem = getMediaItem(currentVideoInfo)

				// reload only when url changed
				if (exoPlayer.currentMediaItem?.localConfiguration?.uri == mediaItem.localConfiguration?.uri) {
					return
				}

				// (Re)load item with correct quality url.
				exoPlayer.stop()
				exoPlayer.clearMediaItems()
				exoPlayer.addAnalyticsListener(playerEventHandler)
				exoPlayer.addMediaItem(mediaItem)
				exoPlayer.prepare()

				if (currentVideoInfo!!.hasDuration) {
					millis = playbackPositionRepository.getPlaybackPosition(currentVideoInfo!!)
				}
			}
		}
	}

	private fun loadStreamQualityByNetworkType() {
		if (currentVideoInfo == null) {
			return
		}

		Timber.w(
			"setStreamQualityByNetworkType - isMetered: %s, quality: %s",
			!networkConnectionHelper.isConnectedToUnmeteredNetwork,
			requiredStreamQualityBucket
		)

		applicationScope.launch(Dispatchers.Main) {
			saveCurrentPlaybackPosition()

			loadStreamQuality(requiredStreamQualityBucket)
		}
	}

	private fun String.toSubtitleMimeType(): String {
		return when {
			endsWith("vtt", true) -> MimeTypes.TEXT_VTT
			endsWith("xml", true) || endsWith("ttml", true) -> MimeTypes.APPLICATION_TTML
			else -> MimeTypes.APPLICATION_TTML
		}
	}
}
