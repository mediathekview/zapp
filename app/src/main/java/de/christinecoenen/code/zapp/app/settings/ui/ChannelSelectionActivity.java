package de.christinecoenen.code.zapp.app.settings.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.woxthebox.draglistview.DragListView;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.databinding.ActivityChannelSelectionBinding;
import de.christinecoenen.code.zapp.models.ISortableChannelList;
import de.christinecoenen.code.zapp.models.json.SortableJsonChannelList;
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager;
import de.christinecoenen.code.zapp.utils.view.SimpleDragListListener;

public class ChannelSelectionActivity extends AppCompatActivity {

	private ISortableChannelList channelList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityChannelSelectionBinding binding = ActivityChannelSelectionBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		DragListView channelListView = binding.draglistChannelSelection;

		// adapter
		channelList = new SortableJsonChannelList(this);
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

	@Override
	protected void onPause() {
		super.onPause();
		channelList.persistChannelOrder();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_channel_selection, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_help:
				openHelpDialog();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void openHelpDialog() {
		DialogFragment newFragment = new ChannelSelectionHelpDialog();
		newFragment.show(getSupportFragmentManager(), "help");
	}
}
