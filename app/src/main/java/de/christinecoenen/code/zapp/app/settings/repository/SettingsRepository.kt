package de.christinecoenen.code.zapp.app.settings.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import de.christinecoenen.code.zapp.R
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class SettingsRepository(context: Context) {

	companion object {
		private const val DEFAULT_WATCHED_SHOW_FADE = 40
		private const val MAX_WATCHED_SHOW_FADE = 80
		private const val WATCHED_SHOW_MODE_HIDE = "hide"
		private const val DEFAULT_MAX_PROCESSED_SHOWS = 5000
		private const val DEFAULT_COLLECTION_CHECK_INTERVAL_MINUTES = 360
	}

	/**
	 * How shows that have been marked as watched are displayed in list overviews.
	 */
	enum class WatchedShowDisplay {
		FADE,
		HIDE
	}

	private val context = context.applicationContext
	val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

	val lockVideosInLandcapeFormat: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_detail_landscape), true)

	val pictureInPictureOnBack: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_pip_on_back), false)

	val unmeteredNetworkStreamQuality: StreamQualityBucket
		get() = preferences.getString(
			context.getString(R.string.pref_key_stream_quality_over_unmetered_network),
			null
		).let { quality ->
			if (quality == null) {
				StreamQualityBucket.HIGHEST
			} else {
				StreamQualityBucket.valueOf(quality.uppercase(Locale.ENGLISH))
			}
		}

	val meteredNetworkStreamQuality: StreamQualityBucket
		get() = preferences.getString(
			context.getString(R.string.pref_key_stream_quality_over_metered_network),
			null
		).let { quality ->
			if (quality == null) {
				StreamQualityBucket.DISABLED
			} else {
				StreamQualityBucket.valueOf(quality.uppercase(Locale.ENGLISH))
			}
		}

	val downloadOverUnmeteredNetworkOnly: Boolean
		get() = preferences.getBoolean(
			context.getString(R.string.pref_key_download_over_unmetered_network_only),
			true
		)

	var isPlayerZoomed: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_player_zoomed), false)
		set(enabled) {
			preferences.edit {
				putBoolean(context.getString(R.string.pref_key_player_zoomed), enabled)
			}
		}

	var sleepTimerDelay: Duration
		get() = preferences.getLong(
			context.getString(R.string.pref_key_sleep_timer_delay),
			30.minutes.inWholeMilliseconds
		).milliseconds
		set(delay) {
			preferences.edit {
				putLong(
					context.getString(R.string.pref_key_sleep_timer_delay),
					delay.inWholeMilliseconds
				)
			}
		}

	val downloadToSdCard: Boolean
		get() = preferences.getBoolean(
			context.getString(R.string.pref_key_download_to_sd_card),
			true
		)

	val dynamicColors: Boolean
		get() = preferences.getBoolean(context.getString(R.string.pref_key_dynamic_colors), false)

	val uiMode: Int
		get() {
			val uiMode = preferences.getString(context.getString(R.string.pref_key_ui_mode), null)
			return prefValueToUiMode(uiMode)
		}

	val startFragment: Int
		get() = when (preferences.getString(
			context.getString(R.string.pref_key_start_tab),
			"live"
		)) {
			"mediathek" -> R.id.mediathekListFragment
			"personal" -> R.id.personalFragment
			"series" -> R.id.seriesFragment
			else -> R.id.channelListFragment
		}

	val searchHistory: Boolean
		get() = preferences.getBoolean(
			context.getString(R.string.pref_key_search_history),
			true
		)

	/**
	 * Whether the playback progress should be shown as a percentage instead of a
	 * circular progress indicator in list overviews.
	 */
	val showProgressPercentage: Boolean
		get() = preferences.getBoolean(
			context.getString(R.string.pref_key_show_progress_percentage),
			false
		)

	/**
	 * Whether shows that are marked as watched should be faded out or hidden
	 * completely in list overviews.
	 */
	val watchedShowDisplay: WatchedShowDisplay
		get() = when (
			preferences.getString(
				context.getString(R.string.pref_key_watched_show_mode),
				null
			)
		) {
			WATCHED_SHOW_MODE_HIDE -> WatchedShowDisplay.HIDE
			else -> WatchedShowDisplay.FADE
		}

	/**
	 * Alpha value (1f = fully visible) that is applied to shows which have been
	 * marked as watched. Derived from the user defined fade strength.
	 */
	/**
	 * Alpha value (1f = fully visible) that is applied to shows which have been
	 * marked as watched. Derived from the user defined fade strength.
	 */
	val watchedShowAlpha: Float
		get() {
			val fadePercent = preferences.getInt(
				context.getString(R.string.pref_key_watched_show_fade),
				DEFAULT_WATCHED_SHOW_FADE
			)

			return 1f - (fadePercent.coerceIn(0, MAX_WATCHED_SHOW_FADE) / 100f)
		}

	/**	 * Playback progress in percent at which a show is automatically marked as watched.
	 * 0 disables the automatic marking.
	 */
	val autoWatchedPercent: Int
		get() = preferences.getInt(
			context.getString(R.string.pref_key_auto_watched_percent),
			0
		).coerceIn(0, 100)

	/**	 * Upper limit of shows that are processed by a single
	 * "mark all as watched/unwatched" action.
	 */
	val maxProcessedShows: Int
		get() = preferences.getString(
			context.getString(R.string.pref_key_collection_max_shows),
			null
		)?.toIntOrNull() ?: DEFAULT_MAX_PROCESSED_SHOWS

	/**
	 * Interval (minutes) in which the collections are checked for new shows.
	 * 0 disables the check.
	 */
	val collectionCheckIntervalMinutes: Int
		get() = preferences.getString(
			context.getString(R.string.pref_key_collection_check_interval),
			null
		)?.toIntOrNull() ?: DEFAULT_COLLECTION_CHECK_INTERVAL_MINUTES

	fun prefValueToUiMode(prefSetting: String?): Int {
		val defaultMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
			AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

		return when (prefSetting) {
			"light" ->
				AppCompatDelegate.MODE_NIGHT_NO

			"dark" ->
				AppCompatDelegate.MODE_NIGHT_YES

			else ->
				defaultMode
		}
	}
}
