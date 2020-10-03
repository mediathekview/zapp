package de.christinecoenen.code.zapp.app.player;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket;

@RunWith(RobolectricTestRunner.class)
public class TrackSelectorWrapperTest {

	private TrackSelectorWrapper trackSelectorWrapper;

	@Before
	public void setup() {
		Context context = ApplicationProvider.getApplicationContext();
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
		trackSelectorWrapper = new TrackSelectorWrapper(trackSelector);
	}

	@Test
	public void testQualities() {
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.DISABLED);
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.LOWEST);
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.MEDIUM);
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.HIGHEST);
	}
}
