package de.christinecoenen.code.zapp.utils.system

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import de.christinecoenen.code.zapp.R
import org.xmlpull.v1.XmlPullParser

object LanguageHelper {

	/**
	 * @return iso language code (without region)
	 */
	fun getCurrentLanguageTag(): String {
		return AppCompatDelegate.getApplicationLocales()[0]?.language ?: "de"
	}

	/**
	 * @return iso langage code mapped to a human readable language label
	 */
	fun getAvailableLanguages(context: Context): Map<String, String> {
		val localeList = getLocaleListFromXml(context)
		val localeMap = mutableMapOf<String, String>()

		for (i in 0 until localeList.size()) {
			val locale = localeList[i]!!
			localeMap[locale.toLanguageTag()] = locale.getDisplayName(locale)
		}

		return localeMap
	}

	private fun getLocaleListFromXml(context: Context): LocaleListCompat {
		val tagsList = mutableListOf<CharSequence>()
		val xmlParser = context.resources.getXml(R.xml.locales_config)

		while (xmlParser.eventType != XmlPullParser.END_DOCUMENT) {
			if (xmlParser.eventType == XmlPullParser.START_TAG && xmlParser.name == "locale") {
				tagsList.add(xmlParser.getAttributeValue(0))
			}
			xmlParser.next()
		}

		return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
	}
}
