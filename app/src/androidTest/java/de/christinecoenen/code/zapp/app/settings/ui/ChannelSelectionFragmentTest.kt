package de.christinecoenen.code.zapp.app.settings.ui

import android.content.pm.ActivityInfo
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.christinecoenen.code.zapp.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChannelSelectionFragmentTest {

	@Test
	fun testRecreation() {
		val scenario = launchFragmentInContainer<ChannelSelectionFragment>(
			themeResId = R.style.AppTheme
		)

		// landscape
		scenario.onFragment {
			it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		}
		scenario.recreate()
	}

}
