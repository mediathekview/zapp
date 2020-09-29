package de.christinecoenen.code.zapp.app.player;

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket;

class TrackSelectorWrapper {

	private final static int MEDIUM_QUALITY_MAX_VIDEO_WIDTH = 640;
	private final static int MEDIUM_QUALITY_MAX_VIDEO_HEIGHT = 360;

	private final DefaultTrackSelector trackSelector;

	public TrackSelectorWrapper(DefaultTrackSelector trackSelector) {
		this.trackSelector = trackSelector;
	}

	public void setStreamQuality(StreamQualityBucket streamQuality) {
		switch (streamQuality) {
			case DISABLED:
			case LOWEST:
				trackSelector.setParameters(trackSelector
					.buildUponParameters()
					.clearVideoSizeConstraints()
					.setForceLowestBitrate(true));
				break;
			case MEDIUM:
				trackSelector.setParameters(trackSelector
					.buildUponParameters()
					.clearVideoSizeConstraints()
					.setMaxVideoSize(MEDIUM_QUALITY_MAX_VIDEO_WIDTH, MEDIUM_QUALITY_MAX_VIDEO_HEIGHT)
					.setForceLowestBitrate(false));
				break;
			case HIGHEST:
				trackSelector.setParameters(trackSelector
					.buildUponParameters()
					.clearVideoSizeConstraints()
					.setForceLowestBitrate(false));
				break;
		}
	}
}
