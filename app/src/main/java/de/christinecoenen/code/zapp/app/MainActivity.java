package de.christinecoenen.code.zapp.app;

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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.about.ui.AboutActivity;
import de.christinecoenen.code.zapp.app.livestream.ui.list.ChannelListFragment;
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragment;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener {

	private static final String ARG_QUERY = "ARG_QUERY";
	private static final int PAGE_CHANNEL_LIST = 0;
	private static final int PAGE_MEDIATHEK_LIST = 1;

	@BindView(R.id.app_bar)
	protected AppBarLayout appBarLayout;

	@BindView(R.id.toolbar)
	protected Toolbar toolbar;

	@BindView(R.id.view_pager)
	protected ViewPager viewPager;

	@BindView(R.id.tab_layout)
	protected TabLayout tabLayout;

	@BindView(R.id.search)
	protected SearchView searchView;

	private String searchQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		viewPager.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
		viewPager.addOnPageChangeListener(this);
		tabLayout.setupWithViewPager(viewPager);

		searchView.setOnQueryTextListener(this);
		searchView.setIconified(false);
		searchView.setIconifiedByDefault(false);

		onPageSelected(viewPager.getCurrentItem());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_QUERY, searchQuery);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		searchQuery = savedInstanceState.getString(ARG_QUERY);
		search(searchQuery);
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

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		searchView.clearFocus();

		switch (position) {
			case PAGE_MEDIATHEK_LIST:
				searchView.setVisibility(View.VISIBLE);
				break;
			case PAGE_CHANNEL_LIST:
			default:
				searchView.setVisibility(View.GONE);
				break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	private void search(String query) {
		Fragment currentFragment = getSupportFragmentManager()
			.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());

		if (currentFragment instanceof MediathekListFragment) {
			((MediathekListFragment) currentFragment).search(query);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		this.searchQuery = newText;
		search(this.searchQuery);
		return true;
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
				case PAGE_CHANNEL_LIST:
					return ChannelListFragment.getInstance();
				case PAGE_MEDIATHEK_LIST:
				default:
					return MediathekListFragment.getInstance();
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.activity_main_tab_live);
				case PAGE_MEDIATHEK_LIST:
				default:
					return getString(R.string.activity_main_tab_mediathek);
			}
		}
	}
}
