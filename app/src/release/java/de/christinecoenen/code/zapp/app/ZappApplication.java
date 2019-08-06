package de.christinecoenen.code.zapp.app;

import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import de.christinecoenen.code.zapp.R;
import timber.log.Timber;

@ReportsCrashes(mailTo = "code.coenen@gmail.com",
	mode = ReportingInteractionMode.DIALOG,
	resDialogText = R.string.error_app_crash,
	resDialogTitle = R.string.app_name,
	resDialogIcon = R.drawable.ic_sad_tv,
	resDialogPositiveButtonText = R.string.action_continue,
	resDialogTheme = R.style.ChrashDialog)
public class ZappApplication extends ZappApplicationBase {

	@Override
	protected void setUpLogging() {
		Timber.plant(new Timber.DebugTree());
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}

}
