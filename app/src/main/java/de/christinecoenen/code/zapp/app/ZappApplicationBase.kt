package de.christinecoenen.code.zapp.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController
import de.christinecoenen.code.zapp.app.player.IPlaybackPositionRepository
import de.christinecoenen.code.zapp.app.player.PersistedPlaybackPositionRepository
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.persistence.Database.Companion.getInstance
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.NotificationHelper.createBackgroundPlaybackChannel
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.ReportField
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat
import timber.log.Timber

@AcraCore(
	buildConfigClass = BuildConfig::class,
	reportFormat = StringFormat.KEY_VALUE_LIST,
	reportContent = [
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
	],
	excludeMatchingSharedPreferencesKeys = [
		"default.acra.legacyAlreadyConvertedToJson",
		"default.acra.lastVersionNr",
		"default.acra.legacyAlreadyConvertedTo4.8.0"
	]
)
@AcraMailSender(
	mailTo = "Zapp Entwicklung <code.coenen@gmail.com>",
	resSubject = R.string.error_app_crash_mail_subject,
	resBody = R.string.error_app_crash_mail_body,
	reportAsFile = false
)
@AcraDialog(
	resText = R.string.error_app_crash,
	resTitle = R.string.app_name,
	resIcon = R.drawable.ic_sad_tv,
	resPositiveButtonText = R.string.action_continue,
	resTheme = R.style.ChrashDialog
)
abstract class ZappApplicationBase : Application() {

	lateinit var channelRepository: ChannelRepository
		private set

	lateinit var mediathekRepository: MediathekRepository
		private set

	lateinit var downloadController: DownloadController
		private set

	lateinit var playbackPositionRepository: IPlaybackPositionRepository
		private set

	fun reportError(throwable: Throwable?) {
		if (ACRA.isInitialised()) {
			ACRA.getErrorReporter().handleException(throwable)
		}

		Timber.e(throwable)
	}

	override fun onCreate() {
		super.onCreate()

		setUpLogging()
		createBackgroundPlaybackChannel(this)

		val settingsRepository = SettingsRepository(this)
		AppCompatDelegate.setDefaultNightMode(settingsRepository.uiMode)

		channelRepository = ChannelRepository(this)

		val database = getInstance(this)

		mediathekRepository = MediathekRepository(database)
		playbackPositionRepository = PersistedPlaybackPositionRepository(mediathekRepository)
		downloadController = DownloadController(this, mediathekRepository)
	}

	protected abstract fun setUpLogging()
	
}
