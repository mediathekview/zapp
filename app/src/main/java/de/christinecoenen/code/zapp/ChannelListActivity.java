package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.adapters.ChannelListAdapter;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.ISortableChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;
import de.christinecoenen.code.zapp.utils.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager;

public class ChannelListActivity extends AppCompatActivity implements ChannelListAdapter.Listener{

	protected @BindView(R.id.toolbar) Toolbar toolbar;
	protected @BindView(R.id.gridview_channels) RecyclerView channelGridView;

	private ISortableChannelList channelList;
	private ChannelListAdapter gridAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_channel_list);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		ViewCompat.setNestedScrollingEnabled(channelGridView, true);

		channelList = new SortableJsonChannelList(this);
		gridAdapter = new ChannelListAdapter(this, channelList, this);
		channelGridView.setLayoutManager(new GridAutofitLayoutManager(this, 350));
		channelGridView.setAdapter(gridAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (MultiWindowHelper.isInsideMultiWindow(this)) {
			resumeActivity();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		channelList.reloadChannelOrder();

		if (!MultiWindowHelper.isInsideMultiWindow(this)) {
			resumeActivity();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!MultiWindowHelper.isInsideMultiWindow(this)) {
			pauseActivity();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (MultiWindowHelper.isInsideMultiWindow(this)) {
			pauseActivity();
		}
	}

	@Override
	public void onItemClick(ChannelModel channel) {
		Intent intent = ChannelDetailActivity.getStartIntent(this, channel.getId());
		startActivity(intent);
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
			case R.id.menu_channel_selection:
				intent = ChannelSelectionActivity.getStartIntent(this);
				startActivity(intent);
				return true;
			case R.id.menu_changelog:
				intent = ChangelogActivity.getStartIntent(this);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void pauseActivity() {
		gridAdapter.pause();
	}

	private void resumeActivity() {
		gridAdapter.resume();
	}
}
