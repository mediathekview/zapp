package de.christinecoenen.code.zapp.utils.video;


import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;

public class TrackHelper {

	private static final String TAG = TrackHelper.class.getSimpleName();

	private final SimpleExoPlayer player;
	private final DefaultTrackSelector trackSelector;

	public TrackHelper(SimpleExoPlayer player, DefaultTrackSelector trackSelector) {
		this.player = player;
		this.trackSelector = trackSelector;
	}

	public void printTrackInfo() {
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo == null) {
			Log.d(TAG, "no track info available");
			return;
		}

		for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.length; rendererIndex++) {
			TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
			int trackType = player.getRendererType(rendererIndex);

			// only use video renderers
			if (trackType == C.TRACK_TYPE_VIDEO) {

				Log.d(TAG, "renderer index: " + rendererIndex);

				for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
					TrackGroup trackGroup = trackGroups.get(groupIndex);


					Log.d(TAG, "track group: " + groupIndex);
					for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
						Format format = trackGroup.getFormat(trackIndex);
						int support = mappedTrackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex);

						if (support == RendererCapabilities.FORMAT_HANDLED) {
							Log.d(TAG, Format.toLogString(format));
						}
					}
				}
			}
		}
	}

	private void switchToFixedTrack(DefaultTrackSelector trackSelector, int rendererIndex, int groupIndex, int trackIndex) {
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);

		MappingTrackSelector.SelectionOverride override = new MappingTrackSelector.SelectionOverride(new FixedTrackSelection.Factory(), groupIndex, trackIndex);
		trackSelector.setSelectionOverride(rendererIndex, trackGroups, override);
	}
}
