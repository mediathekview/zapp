package de.christinecoenen.code.zapp.app.mediathek.ui.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.Objects;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;

public class MediathekListFilterFragment extends Fragment {

	private static final String SHARED_PREFS_KEY_FILTER_OPEN = "SHARED_PREFS_FRILTER_OPEN";

	@BindArray(R.array.mediathek_channels)
	protected String[] channelNames;

	@BindView(R.id.container_channel_buttons)
	protected LinearLayout channelButtonContainer;


	private OnFragmentInteractionListener mListener;
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

	/*@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
				+ " must implement OnFragmentInteractionListener");
		}
	}*/

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
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
			channelButtonContainer,
			false);
		channelButton.setTextOff(channelName);
		channelButton.setTextOn(channelName);
		channelButton.setChecked(true);
		channelButtonContainer.addView(channelButton);
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}
}
