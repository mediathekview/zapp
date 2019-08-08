package de.christinecoenen.code.zapp.app.livestream.ui.list;


import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelDetailActivity;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.ISortableChannelList;
import de.christinecoenen.code.zapp.model.json.SortableVisibleJsonChannelList;
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager;


public class ChannelListFragment extends Fragment implements ChannelListAdapter.Listener {


	public static ChannelListFragment getInstance() {
		return new ChannelListFragment();
	}


	@BindView(R.id.gridview_channels)
	protected RecyclerView channelGridView;


	private ISortableChannelList channelList;
	private ChannelListAdapter gridAdapter;
	private ChannelModel longClickChannel;


	public ChannelListFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		channelList = new SortableVisibleJsonChannelList(getContext());
		gridAdapter = new ChannelListAdapter(getContext(), channelList, this);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_channel_list, container, false);
		ButterKnife.bind(this, view);

		ViewCompat.setNestedScrollingEnabled(channelGridView, true);
		channelGridView.setLayoutManager(new GridAutofitLayoutManager(getContext(), 320));
		channelGridView.setAdapter(gridAdapter);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (MultiWindowHelper.isInsideMultiWindow(getActivity())) {
			resumeActivity();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		channelList.reloadChannelOrder();

		if (!MultiWindowHelper.isInsideMultiWindow(getActivity())) {
			resumeActivity();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (!MultiWindowHelper.isInsideMultiWindow(getActivity())) {
			pauseActivity();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (MultiWindowHelper.isInsideMultiWindow(getActivity())) {
			pauseActivity();
		}
	}

	@Override
	public void onItemClick(ChannelModel channel) {
		Intent intent = ChannelDetailActivity.getStartIntent(getContext(), channel.getId());
		startActivity(intent);
	}

	@Override
	public void onItemLongClick(ChannelModel channel, View view) {
		this.longClickChannel = channel;
		PopupMenu menu = new PopupMenu(getContext(), view, Gravity.TOP | Gravity.END);
		menu.inflate(R.menu.activity_channel_list_context);
		menu.show();
		menu.setOnMenuItemClickListener(this::onContextMenuItemClicked);
	}

	private boolean onContextMenuItemClicked(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_share:
				startActivity(Intent.createChooser(longClickChannel.getVideoShareIntent(), getString(R.string.action_share)));
				return true;
		}
		return false;
	}

	private void pauseActivity() {
		gridAdapter.pause();
	}

	private void resumeActivity() {
		gridAdapter.resume();
	}
}
