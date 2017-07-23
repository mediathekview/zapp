package de.christinecoenen.code.zapp.app.settings.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.woxthebox.draglistview.DragListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ISortableChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager;
import de.christinecoenen.code.zapp.utils.view.SimpleDragListListener;

public class ChannelSelectionActivity extends AppCompatActivity {

	protected @BindView(R.id.draglist_channel_selection) DragListView channelListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_channel_selection);
		ButterKnife.bind(this);

		ActionBar toolbar = getSupportActionBar();
		if (toolbar != null) {
			toolbar.setSubtitle(R.string.activity_channel_selection_subtitle);
		}

		// adapter
		final ISortableChannelList channelList = new SortableJsonChannelList(this);
		final ChannelSelectionAdapter listAdapter = new ChannelSelectionAdapter(this);
		listAdapter.setItemList(channelList.getList());

		// view
		RecyclerView.LayoutManager layoutManager = new GridAutofitLayoutManager(this, 120);
		channelListView.setLayoutManager(layoutManager);
		channelListView.setAdapter(listAdapter, true);
		channelListView.getRecyclerView().setVerticalScrollBarEnabled(true);

		channelListView.setDragListListener(new SimpleDragListListener() {
			@Override
			public void onItemDragEnded(int fromPosition, int toPosition) {
				if (fromPosition != toPosition) {
					channelList.persistChannelOrder();
				}
			}
		});
	}
}
