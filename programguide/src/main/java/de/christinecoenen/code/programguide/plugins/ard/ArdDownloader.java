package de.christinecoenen.code.programguide.plugins.ard;


import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.programguide.plugins.BaseProgramGuideDownloader;

public class ArdDownloader extends BaseProgramGuideDownloader {

	public static final Channel[] CHANNELS = new Channel[] {
			Channel.DAS_ERSTE,
			Channel.BR_NORD,
			Channel.BR_SUED,
			Channel.HR,
			Channel.MDR_SACHSEN,
			Channel.MDR_SACHSEN_ANHALT,
			Channel.MDR_THUERINGEN,
			Channel.NDR_HAMBURG,
			Channel.NDR_MECKLENBURG_VORPOMMERN,
			Channel.NDR_NIEDERSACHSEN,
			Channel.NDR_SCHLESWIG_HOLSTEIN,
			Channel.RBB_BERLIN,
			Channel.RBB_BRANDENBURG,
			Channel.SR,
			Channel.SWR_BADEN_WUERTTEMBERG,
			Channel.SWR_RHEINLAND_PFALZ,
			Channel.WDR,
			Channel.ARD_ALPHA,
			Channel.TAGESSCHAU24,
			Channel.ONE
	};

	private static final String TAG = ArdDownloader.class.getSimpleName();
	private static final String HTML_URL = "http://programm.ard.de/TV/Programm/Load/NavJetztImTV35";

	public ArdDownloader(RequestQueue queue, Channel channel) {
		super(queue, channel);
	}

	@Override
	public void download() {
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
