package de.christinecoenen.code.zapp.app.player;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket;
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class Player {

	private final static String SUBTITLE_LANGUAGE_ON = "deu";

	private final IPlaybackPositionRepository playbackPositionRepository;
	private final SimpleExoPlayer player;
	private final DefaultDataSourceFactory dataSourceFactory;
	private final TrackSelectorWrapper trackSelectorWrapper;
	private final PlayerEventHandler playerEventHandler;
	private final MediaSessionCompat mediaSession;
	private final SettingsRepository settings;
	private final NetworkConnectionHelper networkConnectionHelper;
	private final Handler playerHandler;
	private final CompositeDisposable disposables = new CompositeDisposable();

	private VideoInfo currentVideoInfo;

	public Player(Context context, IPlaybackPositionRepository playbackPositionRepository) {
		this.playbackPositionRepository = playbackPositionRepository;
		settings = new SettingsRepository(context);
		networkConnectionHelper = new NetworkConnectionHelper(context);

		String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
		TransferListener transferListener = new OnlyWifiTransferListener();
		// TODO: we should use this to handle network changes
		dataSourceFactory = new DefaultDataSourceFactory(context, userAgent, transferListener);
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(context, videoTrackSelectionFactory);

		player = new SimpleExoPlayer
			.Builder(context)
			.setTrackSelector(trackSelector)
			.setWakeMode(C.WAKE_MODE_NETWORK)
			.build();

		playerHandler = new Handler(player.getApplicationLooper());
		trackSelectorWrapper = new TrackSelectorWrapper(trackSelector);

		// media session setup
		mediaSession = new MediaSessionCompat(context, context.getPackageName());
		MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
		mediaSessionConnector.setPlayer(player);
		mediaSession.setActive(true);

		// audio focus setup
		AudioAttributes audioAttributes = new AudioAttributes.Builder()
			.setUsage(C.USAGE_MEDIA)
			.setContentType(C.CONTENT_TYPE_MOVIE)
			.build();
		player.setAudioAttributes(audioAttributes, true);

		playerEventHandler = new PlayerEventHandler();

		// set listeners
		networkConnectionHelper.startListenForNetworkChanges(this::setStreamQualityByNetworkType);
	}

	public void setView(StyledPlayerView videoView) {
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
		MediaItem mediaItem = getMediaItem(videoInfo);
		player.stop(true);

		player.addAnalyticsListener(playerEventHandler);

		player.addMediaItem(mediaItem);
		player.prepare();

		if (videoInfo.hasDuration()) {
			Disposable loadPositionDisposable = playbackPositionRepository
				.getPlaybackPosition(currentVideoInfo)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::setMillis, Timber::e);
			disposables.add(loadPositionDisposable);
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
		// TODO: do we still need this?
		/*player.seekTo(
			Math.max(player.getCurrentPosition() - PlayerControlView.DEFAULT_REWIND_MS, 0)
		);*/
	}

	public void fastForward() {
		// TODO: do we still need this?
		/*player.seekTo(
			Math.min(player.getCurrentPosition() + PlayerControlView.DEFAULT_FAST_FORWARD_MS, player.getDuration())
		);*/
	}

	public Observable<Boolean> isBuffering() {
		return playerEventHandler.isBuffering().distinctUntilChanged();
	}

	public boolean isIdle() {
		return player.getPlaybackState() == com.google.android.exoplayer2.Player.STATE_IDLE;
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

	private void saveCurrentPlaybackPosition() {
		if (currentVideoInfo == null) {
			return;
		}

		playbackPositionRepository.savePlaybackPosition(currentVideoInfo, getMillis(), player.getDuration());
	}

	@NonNull
	private MediaItem getMediaItem(VideoInfo videoInfo) {
		StreamQualityBucket quality = getRequiredStreamQualityBucket();
		String uri = videoInfo.getPlaybackUrlOrFilePath(quality);
		MediaItem.Builder mediaItemBuilder = new MediaItem.Builder().setUri(uri);

		// add subtitles if present
		if (videoInfo.hasSubtitles()) {
			MediaItem.Subtitle subtitle =
				new MediaItem.Subtitle(
					Uri.parse(videoInfo.getSubtitleUrl()),
					MimeTypes.TEXT_VTT,
					SUBTITLE_LANGUAGE_ON,
					C.SELECTION_FLAG_AUTOSELECT);

			List<MediaItem.Subtitle> subtitles = new ArrayList<>();
			subtitles.add(subtitle);
			mediaItemBuilder.setSubtitles(subtitles);
		}

		return mediaItemBuilder.build();
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

	private void setStreamQualityByNetworkType() {
		setStreamQuality(getRequiredStreamQualityBucket());
	}

	private StreamQualityBucket getRequiredStreamQualityBucket() {
		if (networkConnectionHelper.isConnectedToWifi() || (currentVideoInfo != null && currentVideoInfo.isOfflineVideo())) {
			return StreamQualityBucket.HIGHEST;
		} else {
			return settings.getCellularStreamQuality();
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
