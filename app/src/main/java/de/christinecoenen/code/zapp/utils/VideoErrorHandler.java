package de.christinecoenen.code.zapp.utils;

import android.media.MediaPlayer;
import android.util.Log;

import de.christinecoenen.code.zapp.R;


public class VideoErrorHandler implements MediaPlayer.OnErrorListener {

	private static final String TAG = VideoErrorHandler.class.getSimpleName();

	private final IVideoErrorListener listener;

	public VideoErrorHandler(IVideoErrorListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		Log.d(TAG, "media player error: " + what + " - " + extra);

		int errorMessageResourceId = R.string.error_stream_unknown;

		switch (what) {
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				Log.e(TAG, "MEDIA_ERROR_UNKNOWN");
				// Unspecified media player error.
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				Log.e(TAG, "MEDIA_ERROR_SERVER_DIED");
				// Media server died. In this case, the application must release
				// the MediaPlayer object and instantiate a new one.
				errorMessageResourceId = R.string.error_stream_server_died;
				break;
		}

		switch (extra) {
			case MediaPlayer.MEDIA_ERROR_IO:
				Log.e(TAG, "MEDIA_ERROR_IO");
				// File or network related operation errors.
				errorMessageResourceId = R.string.error_stream_io;
				break;
			case MediaPlayer.MEDIA_ERROR_MALFORMED:
				Log.e(TAG, "MEDIA_ERROR_MALFORMED");
				// Bitstream is not conforming to the related coding standard or file spec.
				errorMessageResourceId = R.string.error_stream_malformed;
				break;
			case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
				Log.e(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
				// The video is streamed and its container is not valid for progressive playback
				// i.e the video's index (e.g moov atom) is not at the start of the file.
				errorMessageResourceId = R.string.error_stream_progressive_playback;
				break;
			case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
				Log.e(TAG, "MEDIA_ERROR_TIMED_OUT");
				// Some operation takes too long to complete, usually more than 3-5 seconds.
				errorMessageResourceId = R.string.error_stream_timed_out;
				break;
			case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
				Log.e(TAG, "MEDIA_ERROR_UNSUPPORTED");
				// Bitstream is conforming to the related coding standard or file spec,
				// but the media framework does not support the feature.
				errorMessageResourceId = R.string.error_stream_unsupported;
				break;
			case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
				Log.e(TAG, "MEDIA_INFO_BAD_INTERLEAVING");
				// Bad interleaving means that a media has been improperly interleaved or
				// not interleaved at all, e.g has all the video samples first then all the
				// audio ones. Video is playing but a lot of disk seeks may be happening
				break;
		}

		return listener.onVideoError(errorMessageResourceId);
	}

	public interface IVideoErrorListener {
		@SuppressWarnings("SameReturnValue")
		boolean onVideoError(int messageResourceId);
	}
}
