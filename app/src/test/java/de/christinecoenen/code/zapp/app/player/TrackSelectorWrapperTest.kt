package de.christinecoenen.code.zapp.app.player

import android.content.Context
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.test.core.app.ApplicationProvider
import de.christinecoenen.code.zapp.AutoCloseKoinTest
import de.christinecoenen.code.zapp.app.settings.repository.StreamQualityBucket
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrackSelectorWrapperTest : AutoCloseKoinTest() {

	private lateinit var trackSelectorWrapper: TrackSelectorWrapper

	@Before
	fun setup() {
		val context = ApplicationProvider.getApplicationContext<Context>()
		val trackSelector = DefaultTrackSelector(context)
		trackSelectorWrapper = TrackSelectorWrapper(trackSelector)
	}

	@Test
	fun testValidQualities() {
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.LOWEST)
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.MEDIUM)
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.HIGHEST)
	}

	@Test(expected = IllegalArgumentException::class)
	fun testInvalidQualities() {
		trackSelectorWrapper.setStreamQuality(StreamQualityBucket.DISABLED)
	}
}
