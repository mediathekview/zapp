package de.christinecoenen.code.programguide.plugins;


import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Channel;
import de.christinecoenen.code.programguide.plugins.ard.ArdDownloader;
import de.christinecoenen.code.programguide.plugins.deutschewelle.DeutscheWelleDownloader;
import de.christinecoenen.code.programguide.plugins.parliament.ParliamentDownloader;
import de.christinecoenen.code.programguide.plugins.zdf.ZdfDownloader;

public class PluginRegistry {

	private static PluginRegistry instance = null;

	public static PluginRegistry getInstance(Context context) {
		if (instance == null) {
			instance = new PluginRegistry(context);
		}
		return instance;
	}


	private final RequestQueue queue;

	private PluginRegistry(Context context) {
		queue = Volley.newRequestQueue(context);
	}

	public IProgramGuideDownloader getDownloader(Channel channel, ProgramGuideRequest.Listener listener) {
		switch (channel) {
			case DAS_ERSTE:
			case BR_NORD:
			case BR_SUED:
			case HR:
			case MDR_SACHSEN:
			case MDR_SACHSEN_ANHALT:
			case MDR_THUERINGEN:
			case NDR_HAMBURG:
			case NDR_MECKLENBURG_VORPOMMERN:
			case NDR_NIEDERSACHSEN:
			case NDR_SCHLESWIG_HOLSTEIN:
			case RBB_BERLIN:
			case RBB_BRANDENBURG:
			case SR:
			case SWR_BADEN_WUERTTEMBERG:
			case SWR_RHEINLAND_PFALZ:
			case WDR:
			case ARD_ALPHA:
			case TAGESSCHAU24:
			case ONE:
			case KIKA:
				return new ArdDownloader(queue, channel, listener);

			case ZDF:
			case PHOENIX:
			case ZDF_INFO:
			case ZDF_NEO:
			case DREISAT:
			case ARTE:
				return new ZdfDownloader(queue, channel, listener);

			case DEUTSCHE_WELLE:
				return new DeutscheWelleDownloader(queue, channel, listener);

			case PARLAMENTSFERNSEHEN_1:
			case PARLAMENTSFERNSEHEN_2:
				return new ParliamentDownloader(queue, channel, listener);
		}
		return null;
	}
}
