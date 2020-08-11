package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.christinecoenen.code.zapp.app.ZappApplication;
import de.christinecoenen.code.zapp.app.mediathek.MediathekData;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;

@RunWith(AndroidJUnit4.class)
public class MediathekPlayerActivityTest {

	private PersistedMediathekShow persistedShow;

	@Before
	public void Setup() {
		ZappApplication zappApplication = (ZappApplication) InstrumentationRegistry
			.getInstrumentation()
			.getTargetContext()
			.getApplicationContext();

		persistedShow = zappApplication.getMediathekRepository()
			.persistOrUpdateShow(MediathekData.getTestShow())
			.blockingFirst();
	}

	@Test
	public void testRecreation() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Intent intent = MediathekPlayerActivity.getStartIntent(context, persistedShow.getId());

		ActivityScenario<Activity> scenario = ActivityScenario.launch(intent);
		scenario.recreate();

		scenario.onActivity(activity -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
		scenario.recreate();
	}

}
