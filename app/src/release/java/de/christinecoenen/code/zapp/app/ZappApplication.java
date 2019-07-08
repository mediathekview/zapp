package de.christinecoenen.code.zapp.app;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.utils.system.NotificationHelper;
import timber.log.Timber;

@ReportsCrashes(mailTo = "code.coenen@gmail.com",
	mode = ReportingInteractionMode.DIALOG,
	resDialogText = R.string.error_app_crash,
	resDialogTitle = R.string.app_name,
	resDialogIcon = R.drawable.ic_sad_tv,
	resDialogPositiveButtonText = R.string.action_continue,
	resDialogTheme = R.style.ChrashDialog)
public class ZappApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Timber.plant(new Timber.DebugTree());

		NotificationHelper.createBackgroundPlaybackChannel(this);

		SettingsRepository settingsRepository = new SettingsRepository(this);
		AppCompatDelegate.setDefaultNightMode(settingsRepository.getUiMode());
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}

}
