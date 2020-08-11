package de.christinecoenen.code.zapp.app.settings.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ChannelSelectionActivityTest {

	@Test
	public void testRecreation() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Intent intent = new Intent(context, ChannelSelectionActivity.class);

		ActivityScenario<Activity> scenario = ActivityScenario.launch(intent);
		scenario.recreate();

		scenario.onActivity(activity -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
		scenario.recreate();
	}

}
