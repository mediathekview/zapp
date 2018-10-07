package de.christinecoenen.code.zapp.app.player;


import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import de.christinecoenen.code.zapp.R;

public class Player {

	private final SimpleExoPlayer player;
	private final DefaultDataSourceFactory dataSourceFactory;
	private MediaSource videoSource;
	private String currentUrl;

	// TODO: implement subtitle support
	// TODO: implement network connection checker
	// TODO: set title / subtitle for background playback notification
	public Player(Context context) {
		DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		dataSourceFactory = new DefaultDataSourceFactory(context,
			Util.getUserAgent(context, context.getString(R.string.app_name)), bandwidthMeter);
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
	}

	public void setView(PlayerView videoView) {
		videoView.setPlayer(player);
	}

	public void load(String url) {
		if (url.equals(currentUrl)) {
			return;
		}

		currentUrl = url;
		Uri videoUri = Uri.parse(currentUrl);
		videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
			.createMediaSource(videoUri);
		player.stop();
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

	public void setMillis(long millis) {
		player.seekTo(millis);
	}

	public long getMillis() {
		return player.getCurrentPosition();
	}

	SimpleExoPlayer getExoPlayer() {
		return player;
	}

	public void destroy() {
		player.release();
	}
}
