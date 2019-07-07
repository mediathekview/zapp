package de.christinecoenen.code.zapp.app.about.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.ui.LibsActivity;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.utils.system.ConfigurationHelper;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;

public class AboutActivity extends LibsActivity {

	public static Intent getStartIntent(Context context) {
		return new Intent(context, AboutActivity.class);
	}

	private final LibsConfiguration.LibsListener buttonListener = new LibsConfiguration.LibsListenerImpl() {
		@Override
		public boolean onExtraClicked(@NonNull View v, Libs.SpecialButton specialButton) {
			switch (specialButton) {
				case SPECIAL1:
					startActivity(FaqActivity.getStartIntent(AboutActivity.this));
					return true;
				case SPECIAL2:
					startActivity(ChangelogActivity.getStartIntent(AboutActivity.this));
					return true;
				case SPECIAL3:
					IntentHelper.sendMail(AboutActivity.this,
						getString(R.string.support_mail),
						getString(R.string.activity_about_feedback_mail_subject));
					return true;
				default:
					return super.onExtraClicked(v, specialButton);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Libs.ActivityStyle style = Libs.ActivityStyle.LIGHT_DARK_TOOLBAR;
		int theme = R.style.AppTheme_About_Light;

		if (ConfigurationHelper.isInDarkMode(this)) {
			style = Libs.ActivityStyle.DARK;
			theme = R.style.AppTheme_About_Dark;
		}

		Intent intent = new LibsBuilder()
			.withActivityTitle(getString(R.string.activity_about_title))
			.withActivityTheme(theme)
			.withActivityStyle(style)
			.withFields(R.string.class.getFields())
			.withAutoDetect(true)
			.withLibraries("acra", "commonsio")
			.withListener(buttonListener)
			.intent(this);

		setIntent(intent);

		super.onCreate(savedInstanceState);
	}
}
