package de.christinecoenen.code.zapp.app.player;

import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import de.christinecoenen.code.zapp.app.player.TrackSelectorWrapper;
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TrackSelectorWrapperTest {

	private TrackSelectorWrapper trackSelectorWrapper;

	@Before
	public void setup() {
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
		trackSelectorWrapper = new TrackSelectorWrapper(trackSelector);
	}

	@Test
	public void testSubtitles() {
		assertFalse(trackSelectorWrapper.areSubtitlesEnabled());

		trackSelectorWrapper.enableSubtitles(true);
		assertTrue(trackSelectorWrapper.areSubtitlesEnabled());

		trackSelectorWrapper.enableSubtitles(false);
		assertFalse(trackSelectorWrapper.areSubtitlesEnabled());
	}

	@Test
	public void testQualities() {
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.DISABLED);
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.LOWEST);
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.MEDIUM);
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.HIGHEST);
	}
}
