package de.christinecoenen.code.zapp.app.player;


import android.content.Context;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import de.christinecoenen.code.zapp.R;

public class Player {

	private final SimpleExoPlayer player;
	private final DefaultDataSourceFactory dataSourceFactory;
	private VideoInfo currentVideoInfo;
	private MediaSessionCompat mediaSession;

	// TODO: implement subtitle support
	// TODO: implement network connection checker
	public Player(Context context) {
		String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
		dataSourceFactory = new DefaultDataSourceFactory(context, userAgent);
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

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
	}

	public void setView(PlayerView videoView) {
		videoView.setPlayer(player);
	}

	public void load(VideoInfo videoInfo) {
		if (videoInfo.equals(currentVideoInfo)) {
			return;
		}

		currentVideoInfo = videoInfo;
		Uri videoUri = Uri.parse(videoInfo.getUrl());
		MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
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

	VideoInfo getCurrentVideoInfo() {
		return currentVideoInfo;
	}

	MediaSessionCompat getMediaSession() {
		return mediaSession;
	}

	void destroy() {
		player.release();
	}
}
