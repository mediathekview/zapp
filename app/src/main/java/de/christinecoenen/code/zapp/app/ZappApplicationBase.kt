package de.christinecoenen.code.zapp.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.tv.error.CrashActivity
import de.christinecoenen.code.zapp.utils.system.NotificationHelper.createBackgroundPlaybackChannel
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
		createBackgroundPlaybackChannel(this)

		koin = startKoin {
			androidLogger(Level.ERROR)
			androidContext(this@ZappApplicationBase)
			modules(KoinModules.AppModule)
		}.koin

		val settingsRepository = SettingsRepository(this)
		AppCompatDelegate.setDefaultNightMode(settingsRepository.uiMode)

		// apply dynamic colors to all activities if enabled by user
		if (settingsRepository.dynamicColors) {
			DynamicColors.applyToActivitiesIfAvailable(this)
		}
	}

	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)

		if (Thread.getDefaultUncaughtExceptionHandler() != null) {
			// exclude test environments
			setUpCrashReporting()
		}
	}

	protected abstract fun setUpLogging()

	private fun setUpCrashReporting() {
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
				mailTo = "Zapp Entwicklung <code.coenen@gmail.com>"
				subject = getString(R.string.error_app_crash_mail_subject)
				body = getString(R.string.error_app_crash_mail_body)
				reportAsFile = false
				enabled = useAppDialog
			}
		}
	}

}
