package de.christinecoenen.code.programguide.plugins.zappbackend;


import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class ZappBackendDownloader extends BaseProgramGuideDownloader {

	private static final String TAG = ZappBackendDownloader.class.getSimpleName();

	private static final String API_URL = "https://zappbackend.herokuapp.com/v1/shows/";

	public ZappBackendDownloader(RequestQueue queue, Channel channel, ProgramGuideRequest.Listener listener) {
		super(queue, channel, listener);
	}

	@Override
	public void downloadWithoutCache() {
		String url = API_URL + channel.toString();
		request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, "json loaded");
				parse(response);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "error loading json: " + error.getMessage());
				downloaderListener.onRequestError();
			}
		});

		queue.add(request);
	}

	private void parse(JSONObject json) {
		Show show;

		try {
			show = ZappBackendParser.parse(json);
		} catch (JSONException e) {
			Log.d(TAG, e.getMessage());
			downloaderListener.onRequestError();
			return;
		}

		downloaderListener.onRequestSuccess(show);
	}
}
