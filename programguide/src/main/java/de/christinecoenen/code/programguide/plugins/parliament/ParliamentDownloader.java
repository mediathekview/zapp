package de.christinecoenen.code.programguide.plugins.parliament;


import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class ParliamentDownloader extends BaseProgramGuideDownloader {

	public static final Channel[] CHANNELS = new Channel[] {
			Channel.PARLAMENTSFERNSEHEN_1,
			Channel.PARLAMENTSFERNSEHEN_2
	};

	private static final String TAG = ParliamentDownloader.class.getSimpleName();
	private static final String XTML_URL_1 = "https://www.bundestag.de/includes/datasources/tv.xml";
	private static final String XTML_URL_2 = "https://www.bundestag.de/includes/datasources/tv2.xml";

	public ParliamentDownloader(RequestQueue queue, Channel channel) {
		super(queue, channel);
	}

	@Override
	public void download() {
		String url = (channel == Channel.PARLAMENTSFERNSEHEN_1) ? XTML_URL_1 : XTML_URL_2;

		request = new StringRequest(url, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.d(TAG, "xml loaded: " + response);
						parse(response);
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG,  "error loading xml: " + error.getMessage());
				downloaderListener.onRequestError();
			}
		});

		queue.add(request);
	}

	private void parse(String xml) {
		Show show = ParliamentParser.parse(xml);
		downloaderListener.onRequestSuccess(show);
	}
}
