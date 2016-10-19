package de.christinecoenen.code.programguide.plugins.ard;


import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class ArdDownloader extends BaseProgramGuideDownloader {

	private static final String TAG = ArdDownloader.class.getSimpleName();
	private static final String HTML_URL = "http://programm.ard.de/TV/Programm/Load/NavJetztImTV35";

	private static ArdDownloader runningInstance = null;
	private static final List<DownloaderListener> waitingInstancesListeners = new ArrayList<>();
	private static final Object lock = new Object();

	public ArdDownloader(RequestQueue queue, Channel channel, ProgramGuideRequest.Listener listener) {
		super(queue, channel, listener);
	}

	@Override
	public void downloadWithoutCache() {
		synchronized (lock) {
			if (runningInstance == null) {
				Log.d(TAG, "start loading html");
				runningInstance = this;
				waitingInstancesListeners.add(downloaderListener);

				request = new StringRequest(HTML_URL,
						new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								Log.d(TAG, "html loaded");
								parse(response);
							}
						}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(TAG, "error loading html: " + error.getMessage());
						onError();
					}
				});

				queue.add(request);
			} else {
				Log.d(TAG, "loading in progress - wait for other instances");
				waitingInstancesListeners.add(downloaderListener);
			}
		}
	}

	@Override
	public void cancel() {
		boolean doCancel = false;

		synchronized (lock) {
			if (waitingInstancesListeners.contains(downloaderListener)) {
				waitingInstancesListeners.remove(downloaderListener);
			}

			if (runningInstance == this && waitingInstancesListeners.isEmpty()) {
				// not needed any more
				doCancel = true;
				runningInstance = null;
			}
		}

		if (doCancel) {
			super.cancel();
		}
	}

	private void onError() {
		List<DownloaderListener> waitingListeners = onResult();
		for (DownloaderListener waitingListener : waitingListeners) {
			waitingListener.onRequestError();
		}
	}

	private void parse(String html) {
		Map<Channel, Show> shows = ArdParser.parse(html);

		List<DownloaderListener> waitingListeners = onResult();
		for (DownloaderListener waitingListener : waitingListeners) {
			waitingListener.onRequestSuccess(shows);
		}
	}

	private List<DownloaderListener> onResult() {
		synchronized (lock) {
			List<DownloaderListener> waitingListeners = new ArrayList<>();
			waitingListeners.addAll(waitingInstancesListeners);
			waitingInstancesListeners.clear();
			runningInstance = null;
			return waitingListeners;
		}
	}
}
