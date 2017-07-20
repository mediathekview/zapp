package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChannelListActivity extends AppCompatActivity {

	protected @BindView(R.id.app_bar) AppBarLayout appBarLayout;
	protected @BindView(R.id.toolbar) Toolbar toolbar;

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

		setContentView(R.layout.activity_channel_list);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
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
}
