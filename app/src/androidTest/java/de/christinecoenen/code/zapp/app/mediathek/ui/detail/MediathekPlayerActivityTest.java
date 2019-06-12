package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.christinecoenen.code.zapp.app.mediathek.MediathekData;

@RunWith(AndroidJUnit4.class)
public class MediathekPlayerActivityTest {

	@Test
	public void testRecreation() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Intent intent = MediathekPlayerActivity.getStartIntent(context, MediathekData.getTestShow());

		ActivityScenario scenario = ActivityScenario.launch(intent);
		scenario.recreate();

		scenario.onActivity(activity -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
		scenario.recreate();
	}

}
