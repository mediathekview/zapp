package de.christinecoenen.code.zapp.app.app.livestream.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelDetailActivity;

@RunWith(AndroidJUnit4.class)
public class ChannelDetailActivityTest {

	@Test
	public void testRecreation() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Intent intent = ChannelDetailActivity.getStartIntent(context, "das_erste");

		ActivityScenario<Activity> scenario = ActivityScenario.launch(intent);
		scenario.recreate();

		scenario.onActivity(activity -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
		scenario.recreate();
	}

}
