package de.christinecoenen.code.zapp.app.settings.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.json.JsonChannelList;
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper;


public class ShortcutPreference extends MultiSelectListPreference implements Preference.OnPreferenceChangeListener {

	private Context context;
	private IChannelList channelList;

	public ShortcutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public ShortcutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public ShortcutPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ShortcutPreference(Context context) {
		super(context);
		init(context);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			setSummaryToSelectedChannels();
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object selectedValues) {
		@SuppressWarnings("unchecked")
		Set<String> selectedIds = (Set<String>) selectedValues;

		if (saveShortcuts(selectedIds)) {
			// all shortcuts could be saved
			return true;
		} else {
			Toast.makeText(context, R.string.pref_shortcuts_too_many, Toast.LENGTH_LONG).show();
			loadValuesFromShortcuts();
			return false;
		}
	}

	private void init(Context context) {
		this.context = context;

		if (ShortcutHelper.areShortcutsSupported()) {
			loadChannelList();
			setSummaryToSelectedChannels();
			setEnabled(true);
		}
	}

	private void loadChannelList() {
		channelList = new JsonChannelList(context);

		int channelCount = channelList.size();
		List<String> entries = new ArrayList<>(channelCount);
		List<String> values = new ArrayList<>(channelCount);

		for (ChannelModel channel : channelList) {
			entries.add(channel.getName());
			values.add(channel.getId());
		}

		setEntries(entries.toArray(new String[channelCount])); // human readable
		setEntryValues(values.toArray(new String[channelCount])); // ids
		loadValuesFromShortcuts();
	}

	private void loadValuesFromShortcuts() {
		List<String> shortcutIds = ShortcutHelper.getChannelIdsOfShortcuts(context);
		setValues(new HashSet<>(shortcutIds));
	}

	@TargetApi(25)
	private boolean saveShortcuts(Set<String> channelIds) {
		List<ChannelModel> channels = new ArrayList<>(channelIds.size());
		for (String channelId : channelIds) {
			channels.add(channelList.get(channelId));
		}

		return ShortcutHelper.updateShortcutsToChannels(context, channels);
	}

	private void setSummaryToSelectedChannels() {
		if (getValues().size() == 0) {
			setSummary(R.string.pref_shortcuts_summary_limit);
			return;
		}

		List<String> selectedChannelNames = new ArrayList<>(getValues().size());

		// sort values
		for (ChannelModel channel : channelList) {
			if (getValues().contains(channel.getId())) {
				selectedChannelNames.add(channel.getName());
			}
		}

		setSummary(TextUtils.join(", ", selectedChannelNames));
	}
}
