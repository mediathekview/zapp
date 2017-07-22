package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.fragments.ChannelListFragment;
import de.christinecoenen.code.zapp.mediathek.ui.MediathekListFragment;

public class MainActivity extends AppCompatActivity {

	@BindView(R.id.app_bar)
	protected AppBarLayout appBarLayout;

	@BindView(R.id.toolbar)
	protected Toolbar toolbar;

	@BindView(R.id.view_pager)
	protected ViewPager viewPager;

	@BindView(R.id.tab_layout)
	protected TabLayout tabLayout;

	private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			if (!recyclerView.isInTouchMode()) {
				// collapsing toolbar will not collapse by default when
				// navigating with keyboard, so we trigger it here
				// see: https://www.novoda.com/blog/fixing-hiding-appbarlayout-android-tv/
				boolean expand = (dy <= 0);
				appBarLayout.setExpanded(expand, true);
			}
		}
	};

	public void addScrollListener(RecyclerView recyclerView) {
		recyclerView.addOnScrollListener(scrollListener);
	}

	public void removeScrollListener(RecyclerView recyclerView) {
		recyclerView.removeOnScrollListener(scrollListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		viewPager.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
		tabLayout.setupWithViewPager(viewPager);
	}

	@Override
	public void onBackPressed() {
		Fragment currentFragment = getSupportFragmentManager()
			.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());

		if (currentFragment != null && currentFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
			currentFragment.getChildFragmentManager().popBackStack();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_channel_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
			case R.id.menu_about:
				intent = AboutActivity.getStartIntent(this);
				startActivity(intent);
				return true;
			case R.id.menu_settings:
				intent = SettingsActivity.getStartIntent(this);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class MainPageAdapter extends FragmentPagerAdapter {

		MainPageAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return ChannelListFragment.getInstance();
				default:
					return MediathekListFragment.getInstance();
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.activity_main_tab_live);
				default:
					return getString(R.string.activity_main_tab_mediathek);
			}
		}
	}
}
