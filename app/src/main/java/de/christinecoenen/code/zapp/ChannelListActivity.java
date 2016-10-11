package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.GridView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import de.christinecoenen.code.zapp.adapters.ChannelListAdapter;
import de.christinecoenen.code.zapp.model.ISortableChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;

public class ChannelListActivity extends AppCompatActivity {

	protected @BindView(R.id.toolbar) Toolbar toolbar;
	protected @BindView(R.id.gridview_channels) GridView channelGridView;

	private ISortableChannelList channelList;
	private BaseAdapter gridAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_channel_list);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		ViewCompat.setNestedScrollingEnabled(channelGridView, true);

		channelList = new SortableJsonChannelList(this);
		gridAdapter = new ChannelListAdapter(this, channelList);
		channelGridView.setAdapter(gridAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		channelList.reloadChannelOrder();
		gridAdapter.notifyDataSetChanged();
	}

	@OnItemClick(R.id.gridview_channels)
	void onGridItemClick(int position) {
		Intent intent = ChannelDetailActivity.getStartIntent(this, position);
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
}
