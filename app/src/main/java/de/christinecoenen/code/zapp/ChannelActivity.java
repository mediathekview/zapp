package de.christinecoenen.code.zapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.BaseAdapter;
import android.widget.GridView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import de.christinecoenen.code.zapp.adapters.ChannelAdapter;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.XmlResourcesChannelList;

public class ChannelActivity extends AppCompatActivity {

	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.gridview) GridView channelGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_channel);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		ViewCompat.setNestedScrollingEnabled(channelGridView, true);

		IChannelList channelList = new XmlResourcesChannelList(this);
		BaseAdapter gridAdapter = new ChannelAdapter(this, channelList);
		channelGridView.setAdapter(gridAdapter);
	}

	@OnItemClick(R.id.gridview)
	void onGridItemClick(int position) {
		Intent intent = new Intent(this, WatchStreamActivity.class);
		intent.putExtra(WatchStreamActivity.EXTRA_CHANNEL_ID, position);
		startActivity(intent);
	}
}
