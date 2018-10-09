package de.christinecoenen.code.zapp.app.player;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaSessionCompat;

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
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;

import de.christinecoenen.code.zapp.R;
import io.reactivex.Observable;
import timber.log.Timber;

public class Player {

	private final static String SUBTITLE_LANGUAGE_ON = "deu";
	private final static String SUBTITLE_LANGUAGE_OFF = "none";

	private final SimpleExoPlayer player;
	private final DefaultDataSourceFactory dataSourceFactory;
	private DefaultTrackSelector trackSelector;
	private final PlayerEventHandler playerEventHandler;
	private VideoInfo currentVideoInfo;
	private final MediaSessionCompat mediaSession;
	private SharedPreferences preferences;

	// TODO: implement network connection checker
	public Player(Context context) {
		String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
		dataSourceFactory = new DefaultDataSourceFactory(context, userAgent);
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
		trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

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
		player.addAnalyticsListener(playerEventHandler);

		// enable subtitles
		// TODO: move user setting into helper class
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean showSubtitlesPref = preferences.getBoolean("pref_enable_subtitles", false);
		enableSubtitles(showSubtitlesPref);
	}

	public void setView(PlayerView videoView) {
		videoView.setPlayer(player);
	}

	public void load(VideoInfo videoInfo) {
		if (videoInfo.equals(currentVideoInfo)) {
			return;
		}

		currentVideoInfo = videoInfo;
		MediaSource videoSource = getMediaSource(videoInfo);
		player.stop(true);
		player.prepare(videoSource);
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
		return playerEventHandler.isBuffering();
	}

	public boolean isShowingSubtitles() {
		Timber.d(trackSelector.getParameters().preferredTextLanguage);
		return !SUBTITLE_LANGUAGE_OFF.equals(trackSelector.getParameters().preferredTextLanguage);
	}

	public Observable<Integer> getErrorResourceId() {
		return playerEventHandler.getErrorResourceId();
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
		player.release();
		mediaSession.release();
	}

	private void enableSubtitles(boolean enabled) {
		String language = enabled ? SUBTITLE_LANGUAGE_ON : SUBTITLE_LANGUAGE_OFF;
		trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage(language));

		preferences.edit()
			.putBoolean("pref_enable_subtitles", enabled)
			.apply();
	}

	@NotNull
	private MediaSource getMediaSource(VideoInfo videoInfo) {
		Uri uri = Uri.parse(videoInfo.getUrl());
		MediaSource mediaSource = getMediaSourceWithoutSubtitles(uri);

		// add subtitles if present
		if (videoInfo.hasSubtitles()) {
			Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML, C.SELECTION_FLAG_DEFAULT, SUBTITLE_LANGUAGE_ON);
			MediaSource textMediaSource = new SingleSampleMediaSource.Factory(dataSourceFactory)
				.createMediaSource(Uri.parse(videoInfo.getSubtitleUrl()), textFormat, C.TIME_UNSET);
			mediaSource = new MergingMediaSource(mediaSource, textMediaSource);
		}

		Format emptyTextFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML, C.SELECTION_FLAG_DEFAULT, SUBTITLE_LANGUAGE_OFF);
		MediaSource emptyTextMediaSource = new SingleSampleMediaSource.Factory(dataSourceFactory)
			.createMediaSource(Uri.EMPTY, emptyTextFormat, 0);
		mediaSource = new MergingMediaSource(mediaSource, emptyTextMediaSource);

		return mediaSource;
	}

	@NotNull
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
}
