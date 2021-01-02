package de.christinecoenen.code.zapp.app.settings.helper

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

internal class PreferenceHelper(context: Context) {

	companion object {
		private const val SHARED_PEFERENCES_NAME = "ZAPP_SHARED_PREFERENCES"
	}


	private val gson = Gson()
	private val preferences = context.getSharedPreferences(SHARED_PEFERENCES_NAME, Context.MODE_PRIVATE)


	fun saveList(key: String, list: List<String>) {
		val jsonList = gson.toJson(list)

		preferences.edit()
			.putString(key, jsonList)
			.apply()
	}

	fun loadList(key: String): List<String>? {
		if (!preferences.contains(key)) {
			return null
		}

		val jsonList = preferences.getString(key, null)
		val listType = object : TypeToken<ArrayList<String>?>() {}.type

		return gson.fromJson(jsonList, listType)
	}
}
