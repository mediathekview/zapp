package de.christinecoenen.code.programguide.plugins.zdf;


import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class ZdfDownloader extends BaseProgramGuideDownloader {

	private static final String TAG = ZdfDownloader.class.getSimpleName();
	private static final String URL_ZDF_ALL = "http://sofa01.zdf.de/epgservice/v2/all/now/json";

	public ZdfDownloader(RequestQueue queue, Channel channel, ProgramGuideRequest.Listener listener) {
		super(queue, channel, listener);
	}

	@Override
	public void downloadWithoutCache() {

		request = new JsonObjectRequest(Request.Method.GET, URL_ZDF_ALL, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "json loaded: " + response.toString());
						parse(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(TAG,  "error loading json: " + error.getMessage());
						downloaderListener.onRequestError();
					}
				});

		queue.add(request);
	}

	private void parse(JSONObject json) {
		Map<Channel, Show> shows;

		try {
			shows = ZdfParser.parse(json);
		} catch (JSONException e) {
			Log.d(TAG, e.getMessage());
			downloaderListener.onRequestError();
			return;
		}

		downloaderListener.onRequestSuccess(shows);
	}
}
