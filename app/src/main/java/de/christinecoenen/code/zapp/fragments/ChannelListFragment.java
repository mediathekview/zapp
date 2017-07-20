package de.christinecoenen.code.zapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.ChannelDetailActivity;
import de.christinecoenen.code.zapp.MainActivity;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.adapters.ChannelListAdapter;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.ISortableChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;
import de.christinecoenen.code.zapp.utils.MultiWindowHelper;
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager;


public class ChannelListFragment extends Fragment implements ChannelListAdapter.Listener {


	public static ChannelListFragment getInstance() {
		return new ChannelListFragment();
	}


	@BindView(R.id.gridview_channels)
	protected RecyclerView channelGridView;


	private ISortableChannelList channelList;
	private ChannelListAdapter gridAdapter;


	public ChannelListFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		channelList = new SortableJsonChannelList(getContext());
		gridAdapter = new ChannelListAdapter(getContext(), channelList, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

	private void pauseActivity() {
		((MainActivity) getActivity()).removeScrollListener(channelGridView);
		gridAdapter.pause();
	}

	private void resumeActivity() {
		((MainActivity) getActivity()).addScrollListener(channelGridView);
		gridAdapter.resume();
	}
}
