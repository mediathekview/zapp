package de.christinecoenen.code.zapp.app.downloads.ui.list

import android.content.pm.ActivityInfo
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.christinecoenen.code.zapp.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadsFragmentTest {

	@Test
	fun testRecreation() {
		val scenario = launchFragmentInContainer<DownloadsFragment>(
			themeResId = R.style.AppTheme
		)
		onView(withText(R.string.fragment_downloads_no_results))
			.check(matches(isDisplayed()))

		// landscape
		scenario.onFragment {
			it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		}
		scenario.recreate()
		onView(withText(R.string.fragment_downloads_no_results))
			.check(matches(isDisplayed()))
	}

}
