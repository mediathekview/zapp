package de.christinecoenen.code.zapp.app;


import android.content.pm.ActivityInfo;
import android.support.test.espresso.contrib.AccessibilityChecks;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.christinecoenen.code.zapp.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainUiTest {

	@Rule
	public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

	/**
	 * Basic ui test that calls every screen and asserts nothing
	 * is crashing very badly. Run this when you are unsure if your
	 * changes broke something.
	 * <p>
	 * Also, this test will output acessibility errors to logcat.
	 */
	@Test
	public void mainUiTest() throws InterruptedException {
		AccessibilityChecks.enable()
			.setRunChecksFromRootView(true)
			.setThrowExceptionForErrors(false);

		// change to mediathek
		onView(withText(R.string.activity_main_tab_mediathek))
			.check(matches(isDisplayed()))
			.perform(click());

		// scroll down and select a show
		onView(withId(R.id.list))
			.perform(swipeUp())
			.perform(swipeUp())
			.perform(actionOnItemAtPosition(19, click()));

		// press play in detail view
		onView(withId(R.id.btn_play))
			.check(matches(isDisplayed()))
			.perform(click());

		// play pause button on video view
		onView(withId(R.id.exo_pause))
			.check(matches(isDisplayed()))
			.perform(click());

		onView(withId(R.id.exo_play))
			.check(matches(isDisplayed()))
			.perform(click());

		// go back to mediathek
		pressBack();
		pressBack();

		// change back to portrait
		activityTestRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Thread.sleep(500);

		// try to refresh
		onView(withId(R.id.menu_refresh))
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
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		Thread.sleep(200);
		onView(withText(R.string.menu_about))
			.check(matches(isDisplayed()))
			.perform(click());

		// go to FAQ screen
		onView(withText(R.string.activity_faq_title))
			.check(matches(isDisplayed()))
			.perform(click());
		pressBack();

		// go to changelog
		onView(withText(R.string.activity_changelog_title))
			.check(matches(isDisplayed()))
			.perform(click());
		pressBack();

		// back to mediathek
		pressBack();
		Thread.sleep(200);

		// change to live tab
		onView(withText(R.string.activity_main_tab_live))
			.check(matches(isDisplayed()))
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
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		Thread.sleep(200);
		onView(withText(R.string.menu_settings))
			.check(matches(isDisplayed()))
			.perform(click());

		// open channel selection
		onView(withText(R.string.pref_channel_selection_title))
			.perform(click());
	}
}
