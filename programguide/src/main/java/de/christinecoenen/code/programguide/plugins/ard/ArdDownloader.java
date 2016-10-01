package de.christinecoenen.code.programguide.plugins.ard;


import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class ArdDownloader extends BaseProgramGuideDownloader {

	private static final String TAG = ArdDownloader.class.getSimpleName();
	private static final String HTML_URL = "http://programm.ard.de/TV/Programm/Load/NavJetztImTV35";

	public ArdDownloader(RequestQueue queue, Channel channel, ProgramGuideRequest.Listener listener) {
		super(queue, channel, listener);
	}

	@Override
	public void downloadWithoutCache() {
		request = new StringRequest(HTML_URL,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.d(TAG, "html loaded: " + response);
						parse(response);
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG,  "error loading html: " + error.getMessage());
				downloaderListener.onRequestError();
			}
		});

		queue.add(request);
	}

	private void parse(String html) {
		Map<Channel, Show> shows = ArdParser.parse(html);
		downloaderListener.onRequestSuccess(shows);
	}
}
