package de.christinecoenen.code.programguide.plugins.deutschewelle;


import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class DeutscheWelleDownloader extends BaseProgramGuideDownloader {

	public static final Channel[] CHANNELS = new Channel[] {
			Channel.DEUTSCHE_WELLE
	};

	private static final String TAG = DeutscheWelleDownloader.class.getSimpleName();
	private static final String JSON_URL = "http://www.dw.com/api/epg/5?languageId=1";

	public DeutscheWelleDownloader(RequestQueue queue, Channel channel) {
		super(queue, channel);
	}

	@Override
	public void download() {
		request = new JsonObjectRequest(Request.Method.GET, JSON_URL, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
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

	private void parse(JSONObject json) {
		try {
			Show show = DeutscheWelleParser.parse(json);
			downloaderListener.onRequestSuccess(show);
		} catch (JSONException e) {
			Log.d(TAG, e.getMessage());
			downloaderListener.onRequestError();
		}
	}
}
