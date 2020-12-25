package de.christinecoenen.code.zapp.app.main;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.about.ui.AboutActivity;
import de.christinecoenen.code.zapp.app.downloads.ui.list.DownloadsFragment;
import de.christinecoenen.code.zapp.app.livestream.ui.list.ChannelListFragment;
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekSearchSuggestionsProvider;
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragment;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

	private static final String ARG_QUERY = "ARG_QUERY";
	private static final int PAGE_CHANNEL_LIST = 0;
	private static final int PAGE_MEDIATHEK_LIST = 1;
	private static final int PAGE_DOWNLOADS = 2;

	private MainViewModel viewModel;

	private ViewPager2 viewPager;
	private SearchView searchView;
	private BottomNavigationView bottomNavigation;

	private String searchQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		viewModel = new ViewModelProvider(this).get(MainViewModel.class);

		ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		viewPager = binding.viewPager;
		searchView = binding.search;
		bottomNavigation = binding.bottomNavigation;

		setSupportActionBar(binding.toolbar);

		viewPager.setAdapter(new MainPageAdapter(this));
		viewPager.setUserInputEnabled(false);
		viewPager.registerOnPageChangeCallback(new OnPageChangeCallback() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				MainActivity.this.onPageSelected(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		searchView.setOnQueryTextListener(this);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setIconifiedByDefault(false);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.clearFocus();
		searchView.setOnQueryTextFocusChangeListener(this::onSearchQueryTextFocusChangeListener);

		binding.bottomNavigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

		onPageSelected(viewPager.getCurrentItem());

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_QUERY, searchQuery);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		searchQuery = savedInstanceState.getString(ARG_QUERY);
		search(searchQuery);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_toolbar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
		}

		return super.onOptionsItemSelected(item);
	}

	private void onPageSelected(int position) {
		searchView.clearFocus();
		bottomNavigation.getMenu().getItem(0).setChecked(false);
		bottomNavigation.getMenu().getItem(1).setChecked(false);
		bottomNavigation.getMenu().getItem(2).setChecked(false);
		bottomNavigation.getMenu().getItem(position).setChecked(true);

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

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// called by searchView on search commit

			String query = intent.getStringExtra(SearchManager.QUERY);

			searchView.setOnQueryTextListener(null);
			searchView.clearFocus();
			searchView.setQuery(query, false);
			searchView.setOnQueryTextListener(this);

			search(query);

			MediathekSearchSuggestionsProvider.saveQuery(this, query);
		}
	}

	private void search(String query) {
		searchQuery = query;

		Fragment currentFragment = getSupportFragmentManager()
			.findFragmentByTag("f" + viewPager.getCurrentItem());

		if (currentFragment instanceof MediathekListFragment) {
			((MediathekListFragment) currentFragment).search(query);
		}
	}

	private boolean onNavigationItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_live:
				viewPager.setCurrentItem(PAGE_CHANNEL_LIST, false);
				return true;
			case R.id.menu_mediathek:
				viewPager.setCurrentItem(PAGE_MEDIATHEK_LIST, false);
				return true;
			case R.id.menu_downloads:
				viewPager.setCurrentItem(PAGE_DOWNLOADS, false);
				return true;
		}

		return false;
	}

	/*
	 * To open up the soft keyboard on Android (Fire) TV when focusing the SearchView.
	 */
	private void onSearchQueryTextFocusChangeListener(View searchView, boolean hasFocus) {
		if (hasFocus && !searchView.isInTouchMode()) {
			searchView.post(() -> {
				InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_FORCED);
			});
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		search(newText);
		return true;
	}

	private static class MainPageAdapter extends FragmentStateAdapter {

		public MainPageAdapter(@NonNull FragmentActivity fragmentActivity) {
			super(fragmentActivity);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			switch (position) {
				case PAGE_CHANNEL_LIST:
					return ChannelListFragment.getInstance();
				case PAGE_MEDIATHEK_LIST:
					return MediathekListFragment.getInstance();
				case PAGE_DOWNLOADS:
				default:
					return DownloadsFragment.Companion.newInstance();
			}
		}

		@Override
		public int getItemCount() {
			return 3;
		}
	}
}
