package de.christinecoenen.code.zapp.app.player

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrackSelectorWrapperTest {

	private lateinit var trackSelectorWrapper: TrackSelectorWrapper

	@Before
	fun setup() {
		val context = ApplicationProvider.getApplicationContext<Context>()
		val trackSelector = DefaultTrackSelector(context)
		trackSelectorWrapper = TrackSelectorWrapper(trackSelector)
	}

	@Test
	fun testQualities() {
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.DISABLED)
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.LOWEST)
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.MEDIUM)
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.HIGHEST)
	}
}
