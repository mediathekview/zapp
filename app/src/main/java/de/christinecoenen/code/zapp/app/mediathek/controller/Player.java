package de.christinecoenen.code.zapp.app.mediathek.controller;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.utils.system.NetworkConnectionHelper;
import de.christinecoenen.code.zapp.utils.video.VideoBufferingHandler;
import de.christinecoenen.code.zapp.utils.video.VideoErrorHandler;

public class Player {

	private final SimpleExoPlayer player;
	private final VideoErrorHandler videoErrorHandler;
	private final VideoBufferingHandler bufferingHandler;
	private final DefaultTrackSelector trackSelector;
	private final boolean hasSubtitles;
	private final int subtitleRendererIndex;
	private final SharedPreferences preferences;
	private final NetworkConnectionHelper networkConnectionHelper;
	private boolean isShowingSubtitles;
	private MediaSource videoSource;
	private long millis = 0;

	public Player(Context context,
				  MediathekShow show,
				  VideoErrorHandler.IVideoErrorListener errorListener,
				  VideoBufferingHandler.IVideoBufferingListener bufferingListener) {

		networkConnectionHelper = new NetworkConnectionHelper(context);
		networkConnectionHelper.startListenForNetworkChanges(this::onNetworkConnectionChanged);

		DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
			Util.getUserAgent(context, context.getString(R.string.app_name)), bandwidthMeter);
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
		trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

		videoErrorHandler = new VideoErrorHandler(errorListener);
		player.addListener(videoErrorHandler);
		bufferingHandler = new VideoBufferingHandler(bufferingListener);
		player.addListener(bufferingHandler);

		Uri videoUri = Uri.parse(show.getVideoUrl());
		videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
			.createMediaSource(videoUri);

		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean showSubtitlesPref = preferences.getBoolean("pref_enable_subtitles", false);
		hasSubtitles = show.hasSubtitle();

		if (hasSubtitles && showSubtitlesPref) {
			Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML,
				C.SELECTION_FLAG_FORCED, "de");
			MediaSource textMediaSource = new SingleSampleMediaSource.Factory(dataSourceFactory)
				.createMediaSource(Uri.parse(show.getSubtitleUrl()), textFormat, C.TIME_UNSET);
			videoSource = new MergingMediaSource(videoSource, textMediaSource);
			isShowingSubtitles = true;
		}

		subtitleRendererIndex = getRendererIndex(C.TRACK_TYPE_TEXT);
	}

	public void setView(PlayerView videoView) {
		videoView.setPlayer(player);
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public long getMillis() {
		return player.getCurrentPosition();
	}

	public void enableSubtitles() {
		enableSubtitles(true);
	}

	public void disableSubtitles() {
		enableSubtitles(false);
	}

	public void pause() {
		millis = player.getCurrentPosition();
		player.stop();
	}

	public void resume() {
		if (!networkConnectionHelper.isVideoPlaybackAllowed()) {
			videoErrorHandler.onWrongNetworkError();
			return;
		}

		if (player.getPlaybackState() == com.google.android.exoplayer2.Player.STATE_IDLE) {
			player.prepare(videoSource);
			player.seekTo(millis);
			player.setPlayWhenReady(true);
		}
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

	public void destroy() {
		networkConnectionHelper.endListenForNetworkChanges();

		player.removeListener(videoErrorHandler);
		player.removeListener(bufferingHandler);
		player.release();
	}

	public boolean hasSubtitles() {
		return hasSubtitles;
	}

	public boolean isShowingSubtitles() {
		return isShowingSubtitles;
	}

	private void onNetworkConnectionChanged() {
		if (networkConnectionHelper.isVideoPlaybackAllowed()) {
			resume();
			videoErrorHandler.onWrongNetworkErrorInvalid();
		} else {
			pause();
			videoErrorHandler.onWrongNetworkError();
		}
	}

	private void enableSubtitles(boolean enabled) {
		if (subtitleRendererIndex != -1) {
			trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(subtitleRendererIndex, !enabled));
		}
		isShowingSubtitles = enabled;

		preferences.edit()
			.putBoolean("pref_enable_subtitles", enabled)
			.apply();
	}

	private int getRendererIndex(int trackType) {
		for (int i = 0; i < player.getRendererCount(); i++) {
			if (player.getRendererType(i) == trackType) {
				return i;
			}
		}
		return -1;
	}
}
