package de.christinecoenen.code.zapp.utils.system;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelDetailActivity;
import de.christinecoenen.code.zapp.model.ChannelModel;

/**
 * Collection of helper functions to access the ShortcutManager API
 * in a safe way.
 */
@SuppressWarnings("WeakerAccess")
public class ShortcutHelper {

	/**
	 * Adds the given channel as shortcut to the launcher icon.
	 * Only call on api level >= 25.
	 *
	 * @param context to access system services
	 * @param channel channel to create a shortcut for
	 * @return true if the channel could be added
	 */
	@TargetApi(25)
	public static boolean addShortcutForChannel(Context context, ChannelModel channel) {
		ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

		if (shortcutManager == null || shortcutManager.getDynamicShortcuts().size() >= 4) {
			return false;
		}

		ShortcutInfo shortcut = new ShortcutInfo.Builder(context, channel.getId())
			.setShortLabel(channel.getName())
			.setLongLabel(channel.getName())
			.setIcon(Icon.createWithResource(context, channel.getDrawableId()))
			.setIntent(ChannelDetailActivity.getStartIntent(context, channel.getId()))
			.build();

		try {
			return shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcut));
		} catch (IllegalArgumentException e) {
			// too many shortcuts
			return false;
		}
	}

	/**
	 * Updates shorcuts to the given list of channel.
	 *
	 * @param context  to access system services
	 * @param channels new list of channels that should appear as shortcuts
	 * @return true the whole list could be updated
	 */
	@TargetApi(25)
	public static boolean updateShortcutsToChannels(Context context, List<ChannelModel> channels) {
		boolean success = true;

		List<String> existingChannelIds = getChannelIdsOfShortcuts(context);
		List<String> updatedChannelIds = new ArrayList<>(channels.size());

		for (ChannelModel channel : channels) {
			updatedChannelIds.add(channel.getId());
		}

		// remove deleted channels
		for (String existingChannelId : existingChannelIds) {
			// delete if not longer inside updatedChannelIds
			if (!updatedChannelIds.contains(existingChannelId)) {
				removeShortcutForChannel(context, existingChannelId);
			}
		}

		// save added channels
		for (ChannelModel channel : channels) {
			if (!existingChannelIds.contains(channel.getId())) {
				success &= addShortcutForChannel(context, channel);
			}
		}

		return success;
	}

	/**
	 * Removes the given channel as shortcut from the launcher icon.
	 * Only call on api level >= 25.
	 *
	 * @param context   to access system services
	 * @param channelId id of the channel you want to remove from shorcut menu
	 */
	@TargetApi(25)
	public static void removeShortcutForChannel(Context context, String channelId) {
		ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
		if (shortcutManager != null) {
			shortcutManager.removeDynamicShortcuts(Collections.singletonList(channelId));
		}
	}

	@TargetApi(25)
	public static List<String> getChannelIdsOfShortcuts(Context context) {
		ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
		if (shortcutManager == null) {
			return Collections.emptyList();
		}

		List<ShortcutInfo> shortcuts = shortcutManager.getDynamicShortcuts();
		List<String> ids = new ArrayList<>(shortcuts.size());
		for (ShortcutInfo shortcut : shortcuts) {
			ids.add(shortcut.getId());
		}
		return ids;
	}

	/**
	 * Call to report a shortcut used.
	 * You may call this using any api level.
	 *
	 * @param context   to access system services
	 * @param channelId id of the channel that has been selected
	 */
	public static void reportShortcutUsageGuarded(Context context, String channelId) {
		if (areShortcutsSupported()) {
			ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
			if (shortcutManager != null) {
				shortcutManager.reportShortcutUsed(channelId);
			}
		}
	}

	/**
	 * @return true if the current api level supports shortcuts
	 */
	public static boolean areShortcutsSupported() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
	}
}
