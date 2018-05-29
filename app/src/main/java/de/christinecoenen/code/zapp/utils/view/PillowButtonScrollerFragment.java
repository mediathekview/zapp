package de.christinecoenen.code.zapp.utils.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;

public class PillowButtonScrollerFragment extends Fragment {
	private static final String ARG_SHARED_PREFS_PREFIX = "ARG_SHARED_PREFS_PREFIX";
	private static final String ARG_KEY_LIST = "ARG_KEY_LIST";
	private static final String ARG_LABEL_LIST = "ARG_LABEL_LIST";


	@BindView(R.id.container_toggle_buttons)
	protected LinearLayout buttonContainer;


	private String sharedPrefsPrefix;
	private String[] keyList;
	private String[] labelList;

	private Listener listener;
	private SharedPreferences sharedPreferences;

	public PillowButtonScrollerFragment() {
		// Required empty public constructor
	}

	public static PillowButtonScrollerFragment newInstance(String sharedPrefsPrefix, String[] keyList, String[] labelList) {
		PillowButtonScrollerFragment fragment = new PillowButtonScrollerFragment();
		Bundle args = new Bundle();
		args.putString(ARG_SHARED_PREFS_PREFIX, sharedPrefsPrefix);
		args.putStringArray(ARG_KEY_LIST, keyList);
		args.putStringArray(ARG_LABEL_LIST, labelList);
		fragment.setArguments(args);
		return fragment;
	}

	public String[] getExcludedKeys() {
		ArrayList<String> excludedKeys = new ArrayList<>();

		for (int i = 0; i < buttonContainer.getChildCount(); i++) {
			ToggleButton button = (ToggleButton) buttonContainer.getChildAt(i);
			if (!button.isChecked()) {
				excludedKeys.add(keyList[i]);
			}
		}

		return excludedKeys.toArray(new String[excludedKeys.size()]);
	}

	public String[] getIncludedKeys() {
		ArrayList<String> includedKeys = new ArrayList<>();

		for (int i = 0; i < buttonContainer.getChildCount(); i++) {
			ToggleButton button = (ToggleButton) buttonContainer.getChildAt(i);
			if (button.isChecked()) {
				includedKeys.add(keyList[i]);
			}
		}

		return includedKeys.toArray(new String[includedKeys.size()]);
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	@Override
	public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
		super.onInflate(context, attrs, savedInstanceState);

		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PillowButtonScrollerFragment);

		int keyListResId = attributes.getResourceId(R.styleable.PillowButtonScrollerFragment_key_list_res_id, 0);
		int labelListResId = attributes.getResourceId(R.styleable.PillowButtonScrollerFragment_label_list_res_id, 0);
		sharedPrefsPrefix = attributes.getString(R.styleable.PillowButtonScrollerFragment_shared_prefs_prefix);

		if (keyListResId == 0 || labelListResId == 0 || sharedPrefsPrefix == null) {
			throw new IllegalArgumentException("shared_prefs_prefix, key_list_res_id and label_list_res_id habe to set in xml layout");
		}

		keyList = getResources().getStringArray(keyListResId);
		labelList = getResources().getStringArray(labelListResId);

		attributes.recycle();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			sharedPrefsPrefix = getArguments().getString(ARG_SHARED_PREFS_PREFIX);
			keyList = getArguments().getStringArray(ARG_KEY_LIST);
			labelList = getArguments().getStringArray(ARG_LABEL_LIST);
		}

		sharedPreferences = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pillow_button_scroller, container, false);
		ButterKnife.bind(this, view);

		for (int i = 0; i< labelList.length; i++) {
			addButton(inflater, i);
		}

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	private void addButton(LayoutInflater inflater, int index) {
		String label = labelList[index];
		String key = labelList[index];

		ToggleButton channelButton = (ToggleButton) inflater.inflate(
			R.layout.fragment_pillow_button_scroller_toggle_button,
			buttonContainer, false);
		channelButton.setTextOff(label);
		channelButton.setTextOn(label);
		channelButton.setTag(index);

		boolean isChecked = sharedPreferences.getBoolean(sharedPrefsPrefix + "_" + key, true);
		channelButton.setChecked(isChecked);

		channelButton.setOnClickListener(this::onButtonClicked);
		channelButton.setOnLongClickListener(this::onButtonLongClicked);
		buttonContainer.addView(channelButton);
	}

	private void onButtonClicked(View view) {
		ToggleButton button = (ToggleButton) view;
		setChannelButton(button, button.isChecked());

		if (listener != null) {
			listener.onValuesChanged();
		}
	}

	private boolean onButtonLongClicked(View view) {
		ToggleButton button = (ToggleButton) view;
		boolean isOnlyOneChecked = getIncludedKeys().length <= 1;
		boolean doOthersCheck = button.isChecked() && isOnlyOneChecked;

		for (int i = 0; i < buttonContainer.getChildCount(); i++) {
			ToggleButton channelButton = (ToggleButton) buttonContainer.getChildAt(i);
			if (channelButton != button) {
				setChannelButton(channelButton, doOthersCheck);
			}
		}
		setChannelButton(button, true);

		if (listener != null) {
			listener.onValuesChanged();
		}

		return true;
	}

	private void setChannelButton(ToggleButton button, boolean checked) {
		int index = (int) button.getTag();

		sharedPreferences
			.edit()
			.putBoolean(sharedPrefsPrefix + "_" + keyList[index], checked)
			.apply();

		button.setChecked(checked);
	}

	public interface Listener {
		void onValuesChanged();
	}
}
