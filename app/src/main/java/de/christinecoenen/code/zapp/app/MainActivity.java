package de.christinecoenen.code.zapp.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.about.ui.AboutActivity;
import de.christinecoenen.code.zapp.app.livestream.ui.list.ChannelListFragment;
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragment;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.utils.system.MenuHelper;

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

	@BindView(R.id.search)
	protected SearchView searchView;

	@BindView(R.id.nav_view)
	protected NavigationView navigationView;

	@BindView(R.id.layout_drawer)
	protected DrawerLayout drawerLayout;

	private String searchQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

		viewPager.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
		viewPager.addOnPageChangeListener(this);

		searchView.setOnQueryTextListener(this);
		searchView.setIconified(false);
		searchView.setIconifiedByDefault(false);

		navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

		onPageSelected(viewPager.getCurrentItem());
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		MenuHelper.uncheckItems(navigationView.getMenu());
		searchView.clearFocus();

		switch (position) {
			case PAGE_MEDIATHEK_LIST:
				setTitle(R.string.activity_main_tab_mediathek);
				searchView.setVisibility(View.VISIBLE);
				navigationView.getMenu().findItem(R.id.menu_mediathek).setChecked(true);
				break;
			case PAGE_CHANNEL_LIST:
			default:
				setTitle(R.string.activity_main_tab_live);
				searchView.setVisibility(View.GONE);
				navigationView.getMenu().findItem(R.id.menu_live).setChecked(true);
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

	private boolean onNavigationItemSelected(MenuItem menuItem) {
		Intent intent;

		switch (menuItem.getItemId()) {
			case R.id.menu_live:
				viewPager.setCurrentItem(PAGE_CHANNEL_LIST, false);
				drawerLayout.closeDrawers();
				return true;
			case R.id.menu_mediathek:
				viewPager.setCurrentItem(PAGE_MEDIATHEK_LIST, false);
				drawerLayout.closeDrawers();
				return true;
			case R.id.menu_about:
				intent = AboutActivity.getStartIntent(this);
				startActivity(intent);
				drawerLayout.closeDrawers();
				return true;
			case R.id.menu_settings:
				intent = SettingsActivity.getStartIntent(this);
				startActivity(intent);
				drawerLayout.closeDrawers();
				return true;
		}

		return false;
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
			super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@NonNull
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
