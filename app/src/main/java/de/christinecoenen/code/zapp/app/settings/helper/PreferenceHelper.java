package de.christinecoenen.code.zapp.app.settings.helper;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class PreferenceHelper {

	private static final String SHARED_PEFERENCES_NAME = "ZAPP_SHARED_PREFERENCES";

	private final Gson gson = new Gson();
	private final SharedPreferences preferences;

	PreferenceHelper(Context context) {
		preferences = context.getSharedPreferences(SHARED_PEFERENCES_NAME, Context.MODE_PRIVATE);
	}

	void saveList(String key, List list) {
		SharedPreferences.Editor editor = preferences.edit();
		String jsonList = gson.toJson(list);
		editor.putString(key, jsonList);
		editor.apply();
	}

	<T> List<T> loadList(String key) {
		if (preferences.contains(key)) {
			String jsonList = preferences.getString(key, null);
			Type listType = new TypeToken<ArrayList<T>>(){}.getType();
			return gson.fromJson(jsonList, listType);
		} else {
			return null;
		}
	}
}
