package de.christinecoenen.code.zapp.utils;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import java.util.Collections;

import de.christinecoenen.code.zapp.ChannelDetailActivity;
import de.christinecoenen.code.zapp.model.ChannelModel;

/**
 * Collection of helper functions to access the ShortcutManager API
 * in a safe way.
 */
public class ShortcutHelper {

	/**
	 * Adds the given channel as shortcut to the launcher icon.
	 * Only call on api level >= 25.
	 * @param context to access system services
	 * @param channel channel to create a shortcut for
	 * @return true if the channel could be added
     */
	@TargetApi(25)
	public static boolean addShortcutForChannel(Context context, ChannelModel channel) {
		ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
		ShortcutInfo shortcut = new ShortcutInfo.Builder(context, channel.getId())
			.setShortLabel(channel.getName())
			.setLongLabel(channel.getName())
			.setIcon(Icon.createWithResource(context, channel.getDrawableId()))
			.setIntent(ChannelDetailActivity.getStartIntent(context, channel.getId()))
			.build();

		return shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcut));
	}

	/**
	 * Removes the given channel as shortcut from the launcher icon.
	 * Only call on api level >= 25.
	 * @param context    to access system services
	 * @param channelId  id of the channel you want to remove from shorcut menu
     */
	@TargetApi(25)
	public static void removeShortcutForChannel(Context context, String channelId) {
		ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
		shortcutManager.removeDynamicShortcuts(Collections.singletonList(channelId));
	}

	/**
	 * Call to report a shortcut used.
	 * You may call this using any api level.
	 * @param context   to access system services
	 * @param channelId id of the channel that has been selected
     */
	public static void reportShortcutUsageGuarded(Context context, String channelId) {
		if (areShortcutsSupported()) {
			ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
			shortcutManager.reportShortcutUsed(channelId);
		}
	}

	/**
	 * @return true if the current api level supports shortcuts
     */
	public static boolean areShortcutsSupported() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
	}
}
