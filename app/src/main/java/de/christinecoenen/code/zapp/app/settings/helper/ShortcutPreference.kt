package de.christinecoenen.code.zapp.app.settings.helper

import android.annotation.TargetApi
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.channels.IChannelList
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper.areShortcutsSupported
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper.getChannelIdsOfShortcuts
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper.updateShortcutsToChannels
import java.util.*

class ShortcutPreference @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : MultiSelectListPreference(context, attrs), Preference.OnPreferenceChangeListener {

	private lateinit var channelList: IChannelList

	init {
		if (areShortcutsSupported()) {
			loadChannelList()
			setSummaryToSelectedChannels()
			isEnabled = true
		}
	}

	override fun onPreferenceChange(preference: Preference, selectedValues: Any): Boolean {
		@Suppress("UNCHECKED_CAST")
		val selectedIds = selectedValues as Set<String>
		val success = saveShortcuts(selectedIds)

		if (!success) {
			// shortcuts could not be saved
			Toast.makeText(context, R.string.pref_shortcuts_too_many, Toast.LENGTH_LONG).show()
		}

		loadValuesFromShortcuts()
		setSummaryToSelectedChannels()

		return success
	}

	private fun loadChannelList() {
		channelList = JsonChannelList(context)

		val entries = channelList.map { it.name }
		val values = channelList.map { it.id }

		setEntries(entries.toTypedArray()) // human readable
		entryValues = values.toTypedArray() // ids

		loadValuesFromShortcuts()
	}

	private fun loadValuesFromShortcuts() {
		val shortcutIds = getChannelIdsOfShortcuts(context!!)
		values = HashSet(shortcutIds)
	}

	@TargetApi(25)
	private fun saveShortcuts(channelIds: Set<String>): Boolean {
		val channels = channelIds.map { channelList[it] }
		return updateShortcutsToChannels(context!!, channels)
	}

	private fun setSummaryToSelectedChannels() {
		if (values.size == 0) {
			setSummary(R.string.pref_shortcuts_summary_limit)
			return
		}

		val selectedChannelNames = channelList
			.filter { values.contains(it.id) }
			.map { it.name }

		summary = TextUtils.join(", ", selectedChannelNames)
	}
}
