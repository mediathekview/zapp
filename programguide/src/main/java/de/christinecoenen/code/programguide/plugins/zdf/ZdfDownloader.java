package de.christinecoenen.code.programguide.plugins.zdf;


import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class ZdfDownloader extends BaseProgramGuideDownloader {

	private static final String TAG = ZdfDownloader.class.getSimpleName();

	// https://api.zdf.de/cmdm/epg/broadcasts?to=2017-03-11T12%3A58%3A42Z&limit=1&page=1&tvServices=zdf&order=desc
	private static final String API_URL = "https://api.zdf.de/cmdm/epg/broadcasts?to=%s&limit=1&page=1&tvServices=%s&order=desc";

	public ZdfDownloader(RequestQueue queue, Channel channel, ProgramGuideRequest.Listener listener) {
		super(queue, channel, listener);
	}

	@Override
	public void downloadWithoutCache() {
		String dateTime = new DateTime().toString("yyyy-MM-dd'T'HH:mm:ssZZ");
		try {
			dateTime = URLEncoder.encode(dateTime, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String url = String.format(API_URL, dateTime, getChannelUrlParam());
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
		}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> params = new HashMap<>();
				params.put("Host", "api.zdf.de");
				params.put("Accept", "application/vnd.de.zdf.v1.0+json");
				params.put("Api-Auth", "Bearer d2726b6c8c655e42b68b0db26131b15b22bd1a32");
				params.put("Origin", "https://www.zdf.de");
				return params;
			}
		};

		queue.add(request);
	}

	private void parse(JSONObject json) {
		Show show;

		try {
			show = ZdfParser.parse(json);
		} catch (JSONException e) {
			Log.d(TAG, e.getMessage());
			downloaderListener.onRequestError();
			return;
		}

		downloaderListener.onRequestSuccess(show);
	}

	private String getChannelUrlParam() {
		switch (channel) {
			case ZDF:
				return "zdf";
			case ZDF_INFO:
				return "zdfInfo";
			case ZDF_NEO:
				return "ZDFneo";
			case PHOENIX:
				return "phoenix";
			case DREISAT:
				return "3sat";
			case KIKA:
				return "ki.ka";
			case ARTE:
				return "arte";
			default:
				return null;
		}
	}
}
