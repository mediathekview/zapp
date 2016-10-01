package de.christinecoenen.code.programguide.plugins;


import com.android.volley.Request;
import com.android.volley.RequestQueue;

import java.util.Map;

import de.christinecoenen.code.programguide.Cache;
import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Show;

public abstract class BaseProgramGuideDownloader implements IProgramGuideDownloader {

	protected final DownloaderListener downloaderListener = new DownloaderListener() {
		@Override
		public void onRequestError() {
			listener.onRequestError();
		}

		@Override
		public void onRequestSuccess(Map<Channel, Show> shows) {
			Cache.getInstance().save(shows);
			onRequestSuccess(shows.get(channel));
		}

		@Override
		public void onRequestSuccess(Show show) {
			if (show == null) {
				onRequestError();
				return;
			}

			listener.onRequestSuccess(show);
			Cache.getInstance().save(channel, show);
		}
	};

	protected final RequestQueue queue;
	protected Request<?> request;
	protected final Channel channel;

	private final ProgramGuideRequest.Listener listener;


	protected BaseProgramGuideDownloader(RequestQueue queue, Channel channel, ProgramGuideRequest.Listener listener) {
		this.queue = queue;
		this.channel = channel;
		this.listener = listener;
	}

	@Override
	public void download() {
		Show show = Cache.getInstance().getShow(this.channel);

		if (show == null) {
			downloadWithoutCache();
		} else {
			listener.onRequestSuccess(show);
		}
	}

	@Override
	public void cancel() {
		if (request != null) {
			request.cancel();
		}
	}

	protected abstract void downloadWithoutCache();

	@SuppressWarnings("unused")
	protected interface DownloaderListener {
		void onRequestError();
		void onRequestSuccess(Map<Channel, Show> shows);
		void onRequestSuccess(Show shows);
	}
}
