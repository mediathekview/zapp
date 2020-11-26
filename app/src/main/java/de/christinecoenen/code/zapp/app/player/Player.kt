package de.christinecoenen.code.zapp.app.player

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class Player(context: Context, private val playbackPositionRepository: IPlaybackPositionRepository) {

	companion object {

		private const val LANGUAGE_GERMAN = "deu"

	}

	val exoPlayer: SimpleExoPlayer
	val mediaSession: MediaSessionCompat

	var currentVideoInfo: VideoInfo? = null
		private set

	val isBuffering: Observable<Boolean>
		get() = playerEventHandler.isBuffering.distinctUntilChanged()

	val isIdle: Boolean
		get() = exoPlayer.playbackState == Player.STATE_IDLE

	val errorResourceId: Observable<Int?>
		get() = Observable.combineLatest(
			playerEventHandler.errorResourceId,
			playerEventHandler.isIdle,
			{ errorResourceId, isIdle -> if (isIdle) errorResourceId else -1 })


	private val playerEventHandler: PlayerEventHandler = PlayerEventHandler()
	private val settings: SettingsRepository = SettingsRepository(context)
	private val networkConnectionHelper: NetworkConnectionHelper = NetworkConnectionHelper(context)

	private val trackSelectorWrapper: TrackSelectorWrapper
	private val disposables = CompositeDisposable()


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
			setParameters(this
				.buildUponParameters()
				.setPreferredAudioLanguage(LANGUAGE_GERMAN)
				.setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(context)
				.setSelectUndeterminedTextLanguage(true)
				.setDisabledTextTrackSelectionFlags(C.SELECTION_FLAG_DEFAULT))
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
		networkConnectionHelper.startListenForNetworkChanges { setStreamQualityByNetworkType() }
	}

	fun setView(videoView: StyledPlayerView) {
		videoView.player = exoPlayer
	}

	fun load(videoInfo: VideoInfo) {
		if (videoInfo == currentVideoInfo) {
			return
		}

		if (currentVideoInfo != null) {
			saveCurrentPlaybackPosition()
		}

		playerEventHandler.errorResourceId.onNext(-1)
		currentVideoInfo = videoInfo

		val mediaItem = getMediaItem(videoInfo)
		exoPlayer.stop(true)
		exoPlayer.addAnalyticsListener(playerEventHandler)
		exoPlayer.addMediaItem(mediaItem)

		exoPlayer.prepare()

		if (videoInfo.hasDuration) {
			val loadPositionDisposable = playbackPositionRepository
				.getPlaybackPosition(currentVideoInfo!!)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({ this.millis = it }) { Timber.e(it) }

			disposables.add(loadPositionDisposable)
		}

		setStreamQualityByNetworkType()
	}

	fun recreate() {
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
		exoPlayer.seekTo(
			(exoPlayer.currentPosition - DefaultControlDispatcher.DEFAULT_REWIND_MS).coerceAtLeast(0)
		)
	}

	fun fastForward() {
		exoPlayer.seekTo(
			(exoPlayer.currentPosition + DefaultControlDispatcher.DEFAULT_FAST_FORWARD_MS).coerceAtMost(exoPlayer.duration)
		)
	}

	fun destroy() {
		saveCurrentPlaybackPosition()
		disposables.clear()
		networkConnectionHelper.endListenForNetworkChanges()
		exoPlayer.removeAnalyticsListener(playerEventHandler)
		exoPlayer.release()
		mediaSession.release()
	}

	private fun saveCurrentPlaybackPosition() {
		if (currentVideoInfo == null) {
			return
		}
		playbackPositionRepository.savePlaybackPosition(currentVideoInfo!!, millis, exoPlayer.duration)
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
				C.SELECTION_FLAG_AUTOSELECT)

			mediaItemBuilder.setSubtitles(listOf(subtitle))
		}

		return mediaItemBuilder.build()
	}

	private fun setStreamQuality(streamQuality: StreamQualityBucket) {
		when (streamQuality) {
			StreamQualityBucket.DISABLED -> {
				exoPlayer.stop()
				exoPlayer.removeAnalyticsListener(playerEventHandler)
				playerEventHandler.errorResourceId.onNext(R.string.error_stream_not_in_unmetered_network)
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
