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
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;

public class MediathekListFilterFragment extends Fragment {

	private static final String SHARED_PREFS_KEY_FILTER_OPEN = "SHARED_PREFS_FRILTER_OPEN";
	private static final String SHARED_PREFS_KEY_FILTER_CHANNEL_SELECTED_ = "SHARED_PREFS_KEY_FILTER_CHANNEL_SELECTED_";

	@BindArray(R.array.mediathek_channels)
	protected String[] channelNames;

	@BindView(R.id.container_channel_buttons)
	protected LinearLayout channelButtonContainer;


	private Listener listener;
	private SharedPreferences sharedPreferences;

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
		ArrayList<String> excludedChannels = new ArrayList<>();

		for (int i = 0; i < channelButtonContainer.getChildCount(); i++) {
			ToggleButton channelButton = (ToggleButton) channelButtonContainer.getChildAt(i);
			if (!channelButton.isChecked()) {
				excludedChannels.add(channelButton.getText().toString());
			}
		}

		return excludedChannels.toArray(new String[excludedChannels.size()]);
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

		for (String channelName : channelNames) {
			addChannelButton(inflater, channelName);
		}

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

	private void addChannelButton(LayoutInflater inflater, String channelName) {
		ToggleButton channelButton = (ToggleButton) inflater.inflate(
			R.layout.fragment_mediathek_list_filter_toggle_button,
			channelButtonContainer, false);
		channelButton.setTextOff(channelName);
		channelButton.setTextOn(channelName);

		boolean isChecked = sharedPreferences.getBoolean(SHARED_PREFS_KEY_FILTER_CHANNEL_SELECTED_ + channelName, true);
		channelButton.setChecked(isChecked);

		channelButton.setOnClickListener(v -> onChannelButtonClicked((ToggleButton) v, channelName));
		channelButton.setOnLongClickListener(v -> onChannelButtonLongClicked((ToggleButton) v, channelName));
		channelButtonContainer.addView(channelButton);
	}

	private void onChannelButtonClicked(ToggleButton button, String channelName) {
		setChannelButton(button, channelName, button.isChecked());

		if (listener != null) {
			listener.onChannelsChanged();
		}
	}

	private boolean onChannelButtonLongClicked(ToggleButton button, String channelName) {
		boolean isOnlyOneChecked = getExcludedChannels().length >= channelButtonContainer.getChildCount() - 1;
		boolean doOthersCheck = button.isChecked() && isOnlyOneChecked;

		for (int i = 0; i < channelButtonContainer.getChildCount(); i++) {
			ToggleButton channelButton = (ToggleButton) channelButtonContainer.getChildAt(i);
			if (channelButton != button) {
				setChannelButton(channelButton, channelButton.getTextOn().toString(), doOthersCheck);
			}
		}
		setChannelButton(button, channelName, true);

		if (listener != null) {
			listener.onChannelsChanged();
		}

		return true;
	}

	private void setChannelButton(ToggleButton button, String channelName, boolean checked) {
		sharedPreferences
			.edit()
			.putBoolean(SHARED_PREFS_KEY_FILTER_CHANNEL_SELECTED_ + channelName, checked)
			.apply();
		button.setChecked(checked);
	}

	public interface Listener {
		void onChannelsChanged();
	}
}
