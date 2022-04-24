package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChannelDetailActivityTest {

	@Test
	fun testRecreation() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext

		val intent = ChannelPlayerActivity.getStartIntent(context, "das_erste")
		val scenario = ActivityScenario.launch<Activity>(intent)

		scenario.recreate()
		scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
		scenario.recreate()
	}

}
