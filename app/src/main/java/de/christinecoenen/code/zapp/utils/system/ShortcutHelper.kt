package de.christinecoenen.code.zapp.utils.system

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelPlayerActivity
import de.christinecoenen.code.zapp.models.channels.ChannelModel

/**
 * Collection of helper functions to access the ShortcutManager API
 * in a safe way.
 */
object ShortcutHelper {

	/**
	 * Adds the given channel as shortcut to the launcher icon.
	 * Only call on api level >= 25.
	 *
	 * @param context to access system services
	 * @param channel channel to create a shortcut for
	 * @return true if the channel could be added
	 */
	@TargetApi(25)
	fun addShortcutForChannel(context: Context, channel: ChannelModel): Boolean {
		val shortcutManager = context.getSystemService(ShortcutManager::class.java)

		if (shortcutManager == null || shortcutManager.dynamicShortcuts.size >= 4) {
			return false
		}

		val shortcut = ShortcutInfo.Builder(context, channel.id)
			.setShortLabel(channel.name)
			.setLongLabel(channel.name)
			.setIcon(Icon.createWithResource(context, channel.drawableId))
			.setIntent(ChannelPlayerActivity.getStartIntent(context, channel.id))
			.build()

		return try {
			shortcutManager.addDynamicShortcuts(listOf(shortcut))
		} catch (e: IllegalArgumentException) {
			// too many shortcuts
			false
		}
	}

	/**
	 * Updates shorcuts to the given list of channel.
	 *
	 * @param context  to access system services
	 * @param channels new list of channels that should appear as shortcuts
	 * @return true the whole list could be updated
	 */
	@JvmStatic
	@TargetApi(25)
	fun updateShortcutsToChannels(context: Context, channels: List<ChannelModel>): Boolean {
		var success = true
		val existingChannelIds = getChannelIdsOfShortcuts(context)
		val updatedChannelIds = channels.map { it.id }

		// remove deleted channels
		for (existingChannelId in existingChannelIds) {
			// delete if not longer inside updatedChannelIds
			if (!updatedChannelIds.contains(existingChannelId)) {
				removeShortcutForChannel(context, existingChannelId)
			}
		}

		// save added channels
		for (channel in channels) {
			if (!existingChannelIds.contains(channel.id)) {
				success = success and addShortcutForChannel(context, channel)
			}
		}

		return success
	}

	/**
	 * Removes the given channel as shortcut from the launcher icon.
	 * Only call on api level >= 25.
	 *
	 * @param context   to access system services
	 * @param channelId id of the channel you want to remove from shorcut menu
	 */
	@TargetApi(25)
	fun removeShortcutForChannel(context: Context, channelId: String?) {
		context.getSystemService(ShortcutManager::class.java)?.apply {
			removeDynamicShortcuts(listOf(channelId))
		}
	}

	@JvmStatic
	@TargetApi(25)
	fun getChannelIdsOfShortcuts(context: Context): List<String> {
		return context.getSystemService(ShortcutManager::class.java)?.let {
			return it.dynamicShortcuts.map { shortcut ->
				shortcut.id
			}
		} ?: emptyList()
	}

	/**
	 * Call to report a shortcut used.
	 * You may call this using any api level.
	 *
	 * @param context   to access system services
	 * @param channelId id of the channel that has been selected
	 */
	@JvmStatic
	fun reportShortcutUsageGuarded(context: Context, channelId: String?) {
		if (areShortcutsSupported()) {
			context.getSystemService(ShortcutManager::class.java)?.apply {
				reportShortcutUsed(channelId)
			}
		}
	}

	/**
	 * @return true if the current api level supports shortcuts
	 */
	@JvmStatic
	fun areShortcutsSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
}
