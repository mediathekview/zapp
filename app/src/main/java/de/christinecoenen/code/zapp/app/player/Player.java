package de.christinecoenen.code.zapp.app.player;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket;
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class Player {

	private final static String SUBTITLE_LANGUAGE_ON = "deu";
	private final static String SUBTITLE_LANGUAGE_OFF = "none";

	private final static IPlaybackPositionRepository playbackPositionRepository = new MemoryPlaybackPositionRepository();

	private final SimpleExoPlayer player;
	private final DefaultDataSourceFactory dataSourceFactory;
	private final TrackSelectorWrapper trackSelectorWrapper;
	private final PlayerEventHandler playerEventHandler;
	private final MediaSessionCompat mediaSession;
	private final SettingsRepository settings;
	private final NetworkConnectionHelper networkConnectionHelper;
	private final Handler playerHandler;
	private final PlayerWakeLocks playerWakeLocks;
	private final CompositeDisposable disposables = new CompositeDisposable();

	private VideoInfo currentVideoInfo;

	public Player(Context context) {
		settings = new SettingsRepository(context);
		networkConnectionHelper = new NetworkConnectionHelper(context);

		String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
		TransferListener transferListener = new OnlyWifiTransferListener();
		dataSourceFactory = new DefaultDataSourceFactory(context, userAgent, transferListener);
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
		playerHandler = new Handler(player.getApplicationLooper());
		trackSelectorWrapper = new TrackSelectorWrapper(trackSelector);

		// media session setup
		mediaSession = new MediaSessionCompat(context, context.getPackageName());
		MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
		mediaSessionConnector.setPlayer(player, null);
		mediaSession.setActive(true);

		// audio focus setup
		AudioAttributes audioAttributes = new AudioAttributes.Builder()
			.setUsage(C.USAGE_MEDIA)
			.setContentType(C.CONTENT_TYPE_MOVIE)
			.build();
		player.setAudioAttributes(audioAttributes, true);

		playerEventHandler = new PlayerEventHandler();

		// enable subtitles
		enableSubtitles(settings.getEnableSubtitles());

		// wakelocks
		playerWakeLocks = new PlayerWakeLocks(context, "Zapp::Player");
		Disposable wakelockDisposable = playerEventHandler
			.getShouldHoldWakelock()
			.distinctUntilChanged()
			.subscribe(this::shouldHoldWakelockChanged);
		disposables.add(wakelockDisposable);

		// set listeners
		networkConnectionHelper.startListenForNetworkChanges(this::setStreamQualityByNetworkType);
	}

	public void setView(PlayerView videoView) {
		videoView.setPlayer(player);
	}

	public void load(VideoInfo videoInfo) {
		if (videoInfo.equals(currentVideoInfo)) {
			return;
		}

		if (currentVideoInfo != null) {
			saveCurrentPlaybackPosition();
		}

		playerEventHandler.getErrorResourceId().onNext(-1);

		currentVideoInfo = videoInfo;
		MediaSource videoSource = getMediaSource(videoInfo);
		player.stop(true);

		player.addAnalyticsListener(playerEventHandler);
		player.prepare(videoSource);

		if (videoInfo.hasDuration()) {
			long positionMillis = playbackPositionRepository.getPlaybackPosition(currentVideoInfo);
			setMillis(positionMillis);
		}
	}

	public void recreate() {
		VideoInfo oldVideoInfo = currentVideoInfo;
		long oldPosition = getMillis();
		currentVideoInfo = null;
		load(oldVideoInfo);
		setMillis(oldPosition);
	}

	public void pause() {
		player.setPlayWhenReady(false);
	}

	public void resume() {
		player.setPlayWhenReady(true);
	}

	public void rewind() {
		player.seekTo(
			Math.max(player.getCurrentPosition() - PlayerControlView.DEFAULT_REWIND_MS, 0)
		);
	}

	public void fastForward() {
		player.seekTo(
			Math.min(player.getCurrentPosition() + PlayerControlView.DEFAULT_FAST_FORWARD_MS, player.getDuration())
		);
	}

	public void enableSubtitles() {
		enableSubtitles(true);
	}

	public void disableSubtitles() {
		enableSubtitles(false);
	}

	public Observable<Boolean> isBuffering() {
		return playerEventHandler.isBuffering().distinctUntilChanged();
	}

	public boolean isIdle() {
		return player.getPlaybackState() == com.google.android.exoplayer2.Player.STATE_IDLE;
	}

	public boolean isShowingSubtitles() {
		return trackSelectorWrapper.areSubtitlesEnabled();
	}

	public Observable<Integer> getErrorResourceId() {
		return Observable.combineLatest(
			playerEventHandler.getErrorResourceId(),
			playerEventHandler.isIdle(),
			(errorResourceId, isIdle) -> isIdle ? errorResourceId : -1);
	}

	public VideoInfo getCurrentVideoInfo() {
		return currentVideoInfo;
	}

	SimpleExoPlayer getExoPlayer() {
		return player;
	}

	MediaSessionCompat getMediaSession() {
		return mediaSession;
	}

	void destroy() {
		saveCurrentPlaybackPosition();

		playerWakeLocks.destroy();
		disposables.clear();
		networkConnectionHelper.endListenForNetworkChanges();
		player.removeAnalyticsListener(playerEventHandler);
		player.release();
		mediaSession.release();
	}

	private void setMillis(long millis) {
		player.seekTo(millis);
	}

	private long getMillis() {
		return player.getCurrentPosition();
	}

	private void enableSubtitles(boolean enabled) {
		trackSelectorWrapper.enableSubtitles(enabled);
		settings.setEnableSubtitles(enabled);
	}

	private void saveCurrentPlaybackPosition() {
		if (currentVideoInfo == null) {
			return;
		}

		playbackPositionRepository.savePlaybackPosition(currentVideoInfo, getMillis());
	}

	@NonNull
	private MediaSource getMediaSource(VideoInfo videoInfo) {
		StreamQualityBucket quality = getRequiredStreamQualityBucket();
		Uri uri = Uri.parse(videoInfo.getUrl(quality));
		MediaSource mediaSource = getMediaSourceWithoutSubtitles(uri);

		// add subtitles if present
		if (videoInfo.hasSubtitles()) {
			Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML, C.SELECTION_FLAG_DEFAULT, SUBTITLE_LANGUAGE_ON);
			MediaSource textMediaSource = new SingleSampleMediaSource.Factory(dataSourceFactory)
				.setTreatLoadErrorsAsEndOfStream(true)
				.createMediaSource(Uri.parse(videoInfo.getSubtitleUrl()), textFormat, C.TIME_UNSET);
			mediaSource = new MergingMediaSource(mediaSource, textMediaSource);
		}

		// empty subtitle source to switch to
		Format emptyTextFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML, C.SELECTION_FLAG_DEFAULT, SUBTITLE_LANGUAGE_OFF);
		MediaSource emptyTextMediaSource = new SingleSampleMediaSource.Factory(dataSourceFactory)
			.setTreatLoadErrorsAsEndOfStream(true)
			.createMediaSource(Uri.EMPTY, emptyTextFormat, 0);
		mediaSource = new MergingMediaSource(mediaSource, emptyTextMediaSource);

		return mediaSource;
	}

	private void setStreamQuality(StreamQualityBucket streamQuality) {
		switch (streamQuality) {
			case DISABLED:
				player.stop();
				playerEventHandler.getErrorResourceId().onNext(R.string.error_stream_not_in_wifi);
				player.removeAnalyticsListener(playerEventHandler);
				break;
			default:
				trackSelectorWrapper.setStreamQuality(streamQuality);
				break;
		}
	}

	@NonNull
	private MediaSource getMediaSourceWithoutSubtitles(Uri uri) {
		int type = Util.inferContentType(uri);
		switch (type) {
			case C.TYPE_HLS:
				return new HlsMediaSource.Factory(dataSourceFactory)
					.createMediaSource(uri);
			case C.TYPE_OTHER:
				return new ExtractorMediaSource.Factory(dataSourceFactory)
					.createMediaSource(uri);
			case C.TYPE_DASH:
			case C.TYPE_SS:
			default:
				throw new IllegalStateException("Unsupported type: " + type);
		}
	}

	private void setStreamQualityByNetworkType() {
		setStreamQuality(getRequiredStreamQualityBucket());
	}

	private StreamQualityBucket getRequiredStreamQualityBucket() {
		return networkConnectionHelper.isConnectedToWifi() ? StreamQualityBucket.HIGHEST : settings.getCellularStreamQuality();
	}

	private void shouldHoldWakelockChanged(boolean shouldHoldWakelock) {
		if (shouldHoldWakelock) {
			playerWakeLocks.acquire();
		} else {
			playerWakeLocks.release();
		}
	}

	private class OnlyWifiTransferListener implements TransferListener {
		@Override
		public void onTransferInitializing(DataSource source, DataSpec dataSpec, boolean isNetwork) {
			playerHandler.post(Player.this::setStreamQualityByNetworkType);
		}

		@Override
		public void onTransferStart(DataSource source, DataSpec dataSpec, boolean isNetwork) {
		}

		@Override
		public void onBytesTransferred(DataSource source, DataSpec dataSpec, boolean isNetwork, int bytesTransferred) {
		}

		@Override
		public void onTransferEnd(DataSource source, DataSpec dataSpec, boolean isNetwork) {
		}
	}
}
