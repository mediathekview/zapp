package de.christinecoenen.code.zapp.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import org.acra.BuildConfig;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController;
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository;
import de.christinecoenen.code.zapp.app.player.IPlaybackPositionRepository;
import de.christinecoenen.code.zapp.app.player.PersistedPlaybackPositionRepository;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.persistence.Database;
import de.christinecoenen.code.zapp.repositories.ChannelRepository;
import de.christinecoenen.code.zapp.utils.system.NotificationHelper;

@AcraCore(buildConfigClass = BuildConfig.class,
	reportFormat = StringFormat.KEY_VALUE_LIST,
	reportContent = {
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
	},
	excludeMatchingSharedPreferencesKeys = {
		"default.acra.legacyAlreadyConvertedToJson",
		"default.acra.lastVersionNr",
		"default.acra.legacyAlreadyConvertedTo4.8.0"
	})
@AcraMailSender(mailTo = "Zapp Entwicklung <code.coenen@gmail.com>",
	resSubject = R.string.error_app_crash_mail_subject,
	resBody = R.string.error_app_crash_mail_body,
	reportAsFile = false)
@AcraDialog(resText = R.string.error_app_crash,
	resTitle = R.string.app_name,
	resIcon = R.drawable.ic_sad_tv,
	resPositiveButtonText = R.string.action_continue,
	resTheme = R.style.ChrashDialog)
@SuppressWarnings("WeakerAccess")
public abstract class ZappApplicationBase extends Application {

	private ChannelRepository channelRepository;
	private MediathekRepository mediathekRepository;
	private DownloadController downloadController;
	private IPlaybackPositionRepository playbackPositionRepository;


	public ChannelRepository getChannelRepository() {
		return channelRepository;
	}

	public MediathekRepository getMediathekRepository() {
		return mediathekRepository;
	}

	public DownloadController getDownloadController() {
		return downloadController;
	}

	public IPlaybackPositionRepository getPlaybackPositionRepository() {
		return playbackPositionRepository;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpLogging();

		NotificationHelper.createBackgroundPlaybackChannel(this);

		SettingsRepository settingsRepository = new SettingsRepository(this);
		AppCompatDelegate.setDefaultNightMode(settingsRepository.getUiMode());

		channelRepository = new ChannelRepository(this);

		Database database = Database.Companion.getInstance(this);

		mediathekRepository = new MediathekRepository(database);
		playbackPositionRepository = new PersistedPlaybackPositionRepository(mediathekRepository);

		downloadController = new DownloadController(this, mediathekRepository);
	}

	protected abstract void setUpLogging();
}
