package de.christinecoenen.code.zapp.app.player;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Pair;
import androidx.annotation.NonNull;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.*;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import java.util.ArrayList;

public class Player {

	private final static String SUBTITLE_LANGUAGE_ON = "deu";
	private final static String SUBTITLE_LANGUAGE_OFF = "none";

	private final SimpleExoPlayer player;
	private final DefaultDataSourceFactory dataSourceFactory;
	private final DefaultTrackSelector trackSelector;
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
		trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
		playerHandler = new Handler(player.getApplicationLooper());

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

		playerEventHandler = new PlayerEventHandler(trackSelector);

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
		networkConnectionHelper.startListenForNetworkChanges(this::stopIfVideoPlaybackNotAllowed);
	}

	public void setView(PlayerView videoView) {
		videoView.setPlayer(player);
	}

	public void load(VideoInfo videoInfo) {
		if (videoInfo.equals(currentVideoInfo)) {
			return;
		}

		playerEventHandler.getErrorResourceId().onNext(-1);

		currentVideoInfo = videoInfo;
		MediaSource videoSource = getMediaSource(videoInfo);
		player.stop(true);

		player.addAnalyticsListener(playerEventHandler);
		player.prepare(videoSource);
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

	public void setMillis(long millis) {
		player.seekTo(millis);
	}

	public long getMillis() {
		return player.getCurrentPosition();
	}

	public Observable<Boolean> isBuffering() {
		return playerEventHandler.isBuffering().distinctUntilChanged();
	}

	public boolean isIdle() {
		return player.getPlaybackState() == com.google.android.exoplayer2.Player.STATE_IDLE;
	}

	public boolean isShowingSubtitles() {
		Timber.d(trackSelector.getParameters().preferredTextLanguage);
		return !SUBTITLE_LANGUAGE_OFF.equals(trackSelector.getParameters().preferredTextLanguage);
	}

	public void showQualitySettingsDialog(Activity activity) {
		overrideVideoTrackGroup(trackSelector);
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo != null) {
			int rendererIndex = getVideoRendererIndex(trackSelector);
			Pair<AlertDialog, TrackSelectionView> dialogPair =
				TrackSelectionView.getDialog(activity, activity.getString(R.string.video_quality), trackSelector, rendererIndex);
			dialogPair.first.show();
		}
	}

	private int getVideoRendererIndex(DefaultTrackSelector trackSelector) {
		int videoRendererIndex = 0;
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo != null) {
			for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
				TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
				if (trackGroups.length != 0 && player.getRendererType(i) == C.TRACK_TYPE_VIDEO) {
					videoRendererIndex = i;
				}
			}
		}
		return videoRendererIndex;
	}

	private void overrideVideoTrackGroup (DefaultTrackSelector trackSelector) {
		int videoRendererIndex = getVideoRendererIndex(trackSelector);
		TrackGroup sortedVideoTrackGroup = getSortedVideoTrackGroup(getSortedFormatArrayList(trackSelector));
		TrackGroupArray sortedVideoTrackgroupArray = new TrackGroupArray(sortedVideoTrackGroup);
		DefaultTrackSelector.ParametersBuilder builder = trackSelector.getParameters().buildUpon();

	}

	private TrackGroup getSortedVideoTrackGroup(@NonNull ArrayList<Format> formatList) {
		Format [] formats = new Format[formatList.size()];
		for (int i = 0; i < formatList.size(); i++) {
			formats[i] = formatList.get(i);
		}
		return new TrackGroup(formats);
	}

	private ArrayList<Format> getSortedFormatArrayList(DefaultTrackSelector trackSelector)  {
		ArrayList<Format> formatArrayList = new ArrayList<>();
		Format tempFormat = null;
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo != null) {
			for (int trackGroups = 0; trackGroups < mappedTrackInfo.getRendererCount(); trackGroups++) {
				TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(trackGroups);
				if (trackGroupArray.length != 0 && player.getRendererType(trackGroups) == C.TRACK_TYPE_VIDEO) {
					//Trackgroup Array für videos auslesen
					for (int j = 0; j < trackGroupArray.length; j++) {
						TrackGroup trackGroup = trackGroupArray.get(j);
						for (int k = 0; k < trackGroup.length; k++) {
							Format format = trackGroup.getFormat(k);
							if (tempFormat == null) {
								tempFormat = format;
							} else {
								if (tempFormat.bitrate != format.bitrate) {
									formatArrayList.add(format);
									tempFormat = format;
								}
							}
						}
					}
				}
			}
		}
		return formatArrayList;
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
		playerWakeLocks.destroy();
		disposables.clear();
		networkConnectionHelper.endListenForNetworkChanges();
		player.removeAnalyticsListener(playerEventHandler);
		player.release();
		mediaSession.release();
	}

	private void enableSubtitles(boolean enabled) {
		String language = enabled ? SUBTITLE_LANGUAGE_ON : SUBTITLE_LANGUAGE_OFF;
		trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage(language));
		settings.setEnableSubtitles(enabled);
	}

	@NonNull
	private MediaSource getMediaSource(VideoInfo videoInfo) {
		Uri uri = Uri.parse(videoInfo.getUrl());
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

	private void stopIfVideoPlaybackNotAllowed() {
		if (!networkConnectionHelper.isVideoPlaybackAllowed()) {
			player.stop();
			playerEventHandler.getErrorResourceId().onNext(R.string.error_stream_not_in_wifi);
			player.removeAnalyticsListener(playerEventHandler);
		}
	}

	private void shouldHoldWakelockChanged(boolean shouldHoldWakelock) {
		if (shouldHoldWakelock) {
			playerWakeLocks.acquire(PlayerWakeLocks.MAX_WAKELOCK_DURATION);
		} else {
			playerWakeLocks.release();
		}
	}

	private class OnlyWifiTransferListener implements TransferListener {
		@Override
		public void onTransferInitializing(DataSource source, DataSpec dataSpec, boolean isNetwork) {
			playerHandler.post(Player.this::stopIfVideoPlaybackNotAllowed);
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
