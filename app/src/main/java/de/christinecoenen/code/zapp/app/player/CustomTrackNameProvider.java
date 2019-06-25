package de.christinecoenen.code.zapp.app.player;

import android.content.res.Resources;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import de.christinecoenen.code.zapp.R;


public class CustomTrackNameProvider extends DefaultTrackNameProvider {

	private final Resources resources;
	private Format tempFormat = null;

	/**
	 * @param resources Resources from which to obtain strings.
	 */
	CustomTrackNameProvider(Resources resources) {
		super(resources);
		this.resources = resources;
	}

	@Override
	public String getTrackName(Format format) {
		String trackName = super.getTrackName(format);
		if (tempFormat != null && tempFormat.bitrate == format.bitrate) {
			tempFormat = format;
			return trackName + " " + this.resources.getString(R.string.video_format_fallback);
		} else  {
			tempFormat = format;
			return trackName;
		}
	}
}
