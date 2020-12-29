package de.christinecoenen.code.zapp.app;


import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.main.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainUiTest {

	@Rule
	public final ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

	/**
	 * Basic ui test that calls every screen and asserts nothing
	 * is crashing very badly. Run this when you are unsure if your
	 * changes broke something.
	 */
	@Test
	public void mainUiTest() throws InterruptedException {
		// change to mediathek
		onView(withId(R.id.menu_mediathek))
			.perform(click());

		// scroll down and select a show
		Thread.sleep(200);
		onView(withId(R.id.list))
			.perform(scrollToPosition(19))
			.perform(actionOnItemAtPosition(18, click()));

		// press play in detail view
		onView(withId(R.id.play))
			.check(matches(isDisplayed()))
			.perform(click());

		// play pause button on video view
		onView(withId(R.id.exo_play_pause))
			.check(matches(isDisplayed()))
			.perform(click());

		onView(withId(R.id.exo_play_pause))
			.check(matches(isDisplayed()))
			.perform(click());

		// go back to mediathek
		pressBack();
		pressBack();

		// change back to portrait
		activityTestRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Thread.sleep(500);

		// try to refresh
		openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
		Thread.sleep(200);
		onView(withText(R.string.menu_refresh))
			.check(matches(isDisplayed()))
			.perform(click());

		// search for shows
		onView(withId(R.id.search_src_text))
			.check(matches(isDisplayed()))
			.perform(replaceText("test"), closeSoftKeyboard());

		// cancel search
		onView(withId(R.id.search_close_btn))
			.check(matches(isDisplayed()))
			.perform(click());

		// go to about screen
		openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
		Thread.sleep(200);
		onView(withText(R.string.menu_about))
			.check(matches(isDisplayed()))
			.perform(click());

		// go to changelog
		onView(withText(R.string.activity_changelog_title))
			.check(matches(isDisplayed()))
			.perform(click());

		// back to mediathek
		pressBack();
		pressBack();
		Thread.sleep(200);

		// go to about screen
		openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
		Thread.sleep(200);
		onView(withText(R.string.menu_about))
			.check(matches(isDisplayed()))
			.perform(click());

		// go to FAQ screen
		onView(withText(R.string.activity_faq_title))
			.check(matches(isDisplayed()))
			.perform(click());
		pressBack();
		Thread.sleep(200);

		// back to mediathek
		pressBack();
		Thread.sleep(200);

		// change to live tab
		onView(withId(R.id.menu_live))
			.perform(click());

		// select a channel
		onView(withId(R.id.gridview_channels))
			.perform(actionOnItemAtPosition(0, click()));

		onView(withId(R.id.fullscreen_content))
			.perform(swipeLeft())
			.perform(swipeLeft());

		// back live tab
		pressBack();

		// go to settings screen
		Thread.sleep(200);
		onView(withId(R.id.menu_settings))
			.check(matches(isDisplayed()))
			.perform(click());
	}
}
