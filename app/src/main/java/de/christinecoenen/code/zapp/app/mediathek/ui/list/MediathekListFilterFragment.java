package de.christinecoenen.code.zapp.app.mediathek.ui.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.utils.view.PillowButtonScrollerFragment;

public class MediathekListFilterFragment extends Fragment {

	private static final String SHARED_PREFS_KEY_FILTER_OPEN = "SHARED_PREFS_FRILTER_OPEN";


	private Listener listener;
	private SharedPreferences sharedPreferences;
	private PillowButtonScrollerFragment channelFilter;
	private PillowButtonScrollerFragment lengthFilter;

	public MediathekListFilterFragment() {
		// Required empty public constructor
	}

	public void toggle() {
		if (isOpen()) {
			close();
		} else {
			open();
		}
	}

	public int getMenuIconResId() {
		return isOpen() ? R.drawable.ic_keyboard_arrow_up_white_24dp : R.drawable.ic_tune_white_24dp;
	}

	public String[] getExcludedChannels() {
		return channelFilter.getExcludedKeys();
	}

	public int getMinLength() {
		// TODO: expose length intervals
		return 0;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mediathek_list_filter, container, false);
		ButterKnife.bind(this, view);

		channelFilter = (PillowButtonScrollerFragment) getChildFragmentManager()
			.findFragmentById(R.id.fragment_channel_filter);
		channelFilter.setListener(() -> listener.onChannelsChanged());

		lengthFilter = (PillowButtonScrollerFragment) getChildFragmentManager()
			.findFragmentById(R.id.fragment_length_filter);
		lengthFilter.setListener(() -> listener.onLengthChanged());

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setOpenOrClosedFromMemory();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (getParentFragment() instanceof Listener) {
			listener = (Listener) getParentFragment();
		} else {
			throw new RuntimeException("Parent fragment must implement listener.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	private void setOpenOrClosedFromMemory() {
		boolean shouldBeOpen = sharedPreferences.getBoolean(SHARED_PREFS_KEY_FILTER_OPEN, false);
		if (shouldBeOpen) {
			open();
		} else {
			close();
		}
	}

	private boolean isOpen() {
		View rootView = Objects.requireNonNull(getView());
		return rootView.getVisibility() == View.VISIBLE;
	}

	private void open() {
		View rootView = Objects.requireNonNull(getView());
		rootView.setVisibility(View.VISIBLE);
		sharedPreferences.edit().putBoolean(SHARED_PREFS_KEY_FILTER_OPEN, true).apply();
	}

	private void close() {
		View rootView = Objects.requireNonNull(getView());
		rootView.setVisibility(View.GONE);
		sharedPreferences.edit().putBoolean(SHARED_PREFS_KEY_FILTER_OPEN, false).apply();
	}

	public interface Listener {
		void onLengthChanged();

		void onChannelsChanged();
	}
}
