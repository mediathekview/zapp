package de.christinecoenen.code.zapp.app.mediathek.ui.detail

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.christinecoenen.code.zapp.app.mediathek.MediathekData
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.player.MediathekPlayerActivity
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
class MediathekPlayerActivityTest : KoinTest {

	private val mediathekRepository: MediathekRepository by inject()
	private lateinit var persistedShow: PersistedMediathekShow

	@Before
	fun setup() {
		persistedShow = mediathekRepository
			.persistOrUpdateShow(MediathekData.testShow)
			.blockingFirst()
	}

	@Test
	fun testRecreation() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext

		val intent = MediathekPlayerActivity.getStartIntent(context, persistedShow.id)
		val scenario = ActivityScenario.launch<Activity>(intent)

		scenario.recreate()
		scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
		scenario.recreate()
	}
}
