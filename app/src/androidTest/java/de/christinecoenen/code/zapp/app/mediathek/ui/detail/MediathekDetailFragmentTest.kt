package de.christinecoenen.code.zapp.app.mediathek.ui.detail

import android.content.pm.ActivityInfo
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.MediathekData
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediathekDetailFragmentTest {

	@Test
	fun testRecreation() {
		val args = MediathekDetailFragmentArgs(MediathekData.testShow)
		val scenario = launchFragmentInContainer<MediathekDetailFragment>(
			fragmentArgs = args.toBundle(),
			themeResId = R.style.AppTheme
		)

		// landscape
		scenario.onFragment {
			it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		}
		scenario.recreate()
	}

}
