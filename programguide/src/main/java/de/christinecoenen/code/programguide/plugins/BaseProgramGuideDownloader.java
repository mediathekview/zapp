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
			onRequestSuccess(shows.get(channel));
			Cache.getInstance().save(shows);
		}

		@Override
		public void onRequestSuccess(Show show) {
			listener.onRequestSuccess(show);
			Cache.getInstance().save(channel, show);
		}
	};

	protected final RequestQueue queue;
	protected Request<?> request;

	private final Channel channel;
	private ProgramGuideRequest.Listener listener;


	protected BaseProgramGuideDownloader(RequestQueue queue, Channel channel) {
		this.queue = queue;
		this.channel = channel;
	}

	@Override
	public void download(ProgramGuideRequest.Listener listener) {
		this.listener = listener;
		Show show = Cache.getInstance().getShow(this.channel);

		if (show == null) {
			download();
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

	protected abstract void download();

	@SuppressWarnings("unused")
	protected interface DownloaderListener {
		void onRequestError();
		void onRequestSuccess(Map<Channel, Show> shows);
		void onRequestSuccess(Show shows);
	}
}
