package de.christinecoenen.code.zapp.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginRepository {

	private static String ACTION_CAST_PLUGIN = "de.christinecoenen.code.zapp.plugin.CAST";

	private final PackageManager packageManager;

	public PluginRepository(PackageManager packageManager) {
		this.packageManager = packageManager;
	}

	public Collection<Plugin> getCastPlugins() {
		Intent intent = new Intent(ACTION_CAST_PLUGIN);

		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
			PackageManager.GET_RESOLVED_FILTER);

		Map<String, Plugin> plugins = new HashMap<>();
		for (ResolveInfo info : list) {
			Plugin plugin = new Plugin();
			plugin.setLabel(info.loadLabel(packageManager).toString());
			plugin.setPackageName(info.activityInfo.applicationInfo.packageName);
			plugins.put(plugin.getPackageName(), plugin);
		}

		return plugins.values();
	}
}
