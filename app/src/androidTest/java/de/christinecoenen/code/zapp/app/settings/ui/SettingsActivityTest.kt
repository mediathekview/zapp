package de.christinecoenen.code.zapp.app.settings.ui

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

	@Test
	fun testRecreation() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext

		val intent = SettingsActivity.getStartIntent(context)
		val scenario = ActivityScenario.launch<Activity>(intent)

		scenario.recreate()
	}
}
