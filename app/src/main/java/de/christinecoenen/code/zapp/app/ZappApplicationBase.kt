package de.christinecoenen.code.zapp.app

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.personal.series.CollectionUpdateScheduler
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.tv.error.CrashActivity
import de.christinecoenen.code.zapp.utils.system.NotificationHelper.createBackgroundPlaybackChannel
import de.christinecoenen.code.zapp.utils.system.NotificationHelper.createCollectionUpdateChannel
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import java.io.File

abstract class ZappApplicationBase : Application() {

	val channelRepository: ChannelRepository
		get() = koin.get()

	val mediathekRepository: MediathekRepository
		get() = koin.get()

	private lateinit var koin: Koin

	@Suppress("unused")
	fun reportError(throwable: Throwable?) {
		if (ACRA.isInitialised) {
			ACRA.errorReporter.handleException(throwable)
		}

		Timber.e(throwable)
	}

	override fun onCreate() {
		super.onCreate()

		setUpLogging()

		if (!isMainProcess()) {
			// activities like the crash report dialog run in their own process and must not
			// initialize the app (Koin, WorkManager, ...) - they do not need it
			return
		}

		createBackgroundPlaybackChannel(this)
		createCollectionUpdateChannel(this)

		koin = startKoin {
			androidLogger(Level.ERROR)
			androidContext(this@ZappApplicationBase)
			modules(KoinModules.AppModule)
		}.koin

		val settingsRepository = SettingsRepository(this)
		AppCompatDelegate.setDefaultNightMode(settingsRepository.uiMode)

		// check the collections for new shows in the background
		CollectionUpdateScheduler.schedule(this, settingsRepository.collectionCheckIntervalMinutes)

		// apply dynamic colors to all activities if enabled by user
		if (settingsRepository.dynamicColors) {
			DynamicColors.applyToActivitiesIfAvailable(this)
		}
	}

	/**
	 * Whether the app runs in its main process.
	 */
	private fun isMainProcess(): Boolean {
		return currentProcessName()?.let { it == packageName } ?: true
	}

	private fun currentProcessName(): String? {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			return Application.getProcessName()
		}

		return try {
			File("/proc/self/cmdline")
				.readText()
				.substringBefore('\u0000')
				.ifBlank { null }
		} catch (e: Exception) {
			Timber.e(e, "Could not determine the process name")
			null
		}
	}

	override fun attachBaseContext(base: Context) {
		super.attachBaseContext(base)

		if (Thread.getDefaultUncaughtExceptionHandler() != null) {
			// exclude test environments
			setUpCrashReporting(base)
		}
	}

	protected abstract fun setUpLogging()

	private fun setUpCrashReporting(context: Context) {
		val useLeanbackDialog = resources.getBoolean(R.bool.is_leanback_ui)
		val useAppDialog = !useLeanbackDialog

		initAcra {
			buildConfigClass = BuildConfig::class.java
			reportFormat = StringFormat.KEY_VALUE_LIST
			reportContent = listOf(
				ReportField.REPORT_ID,
				ReportField.USER_EMAIL,
				ReportField.USER_COMMENT,
				ReportField.IS_SILENT,
				ReportField.USER_CRASH_DATE,
				ReportField.APP_VERSION_NAME,
				ReportField.APP_VERSION_CODE,
				ReportField.ANDROID_VERSION,
				ReportField.PHONE_MODEL,
				ReportField.BRAND,
				ReportField.SHARED_PREFERENCES,
				ReportField.STACK_TRACE
			)
			excludeMatchingSharedPreferencesKeys = listOf(
				"default.acra.legacyAlreadyConvertedToJson",
				"default.acra.lastVersionNr",
				"default.acra.legacyAlreadyConvertedTo4.8.0"
			)

			if (useAppDialog) {
				dialog {
					text = getString(R.string.error_app_crash)
					title = getString(R.string.app_name)
					resIcon = R.drawable.ic_sad_tv
					positiveButtonText = getString(R.string.action_continue)
					resTheme = R.style.AppTheme
					enabled = true
				}
			}

			if (useLeanbackDialog) {
				dialog {
					title = getString(R.string.error_informal)
					text = getString(R.string.error_app_crash_tv)
					resIcon = R.drawable.ic_sad_tv
					resTheme = R.style.LeanbackAppTheme
					reportDialogClass = CrashActivity::class.java
					enabled = true
				}
			}

			mailSender {
				mailTo = "Zapp Entwicklung <${context.getString(R.string.support_mail)}>"
				subject = getString(R.string.error_app_crash_mail_subject)
				body = getString(R.string.error_app_crash_mail_body)
				reportAsFile = false
				enabled = useAppDialog
			}
		}
	}

}
