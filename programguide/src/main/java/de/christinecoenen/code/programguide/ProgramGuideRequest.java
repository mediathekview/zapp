package de.christinecoenen.code.programguide;


import android.content.Context;
import android.util.Log;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.IProgramGuideDownloader;
import de.christinecoenen.code.programguide.plugins.PluginRegistry;

public class ProgramGuideRequest {

	private static final String TAG = ProgramGuideRequest.class.getSimpleName();

	private final Context context;
	private Listener listener;
	private Channel channelId;
	private IProgramGuideDownloader downloader;

	public ProgramGuideRequest(Context context) {
		this.context = context;
	}

	public ProgramGuideRequest setListener(Listener listener) {
		this.listener = listener;
		return this;
	}

	@SuppressWarnings("WeakerAccess")
	public ProgramGuideRequest setChannelId(Channel channelId) {
		this.channelId = channelId;
		return this;
	}

	public ProgramGuideRequest setChannelId(String channelId) {
		Channel newChannel = null;
		try {
			newChannel = Channel.getById(channelId);
		} catch (IllegalArgumentException e) {
			Log.w(TAG, channelId + " is no valid channel id");
		}

		return setChannelId(newChannel);
	}

	public ProgramGuideRequest execute() {
		if (listener == null) {
			throw new RuntimeException("listener not set");
		}

		if (channelId == null) {
			Log.w(TAG, "no valid channel id set");
			listener.onRequestError();
			return this;
		}

		downloader = PluginRegistry.getInstance(context).getDownloader(channelId);

		if (downloader == null) {
			listener.onRequestError();
		} else {
			downloader.download(listener);
		}

		return this;
	}

	public void cancel() {
		if (downloader != null) {
			downloader.cancel();
		}
	}

	public interface Listener {
		void onRequestError();
		void onRequestSuccess(Show currentShow);
	}
}
