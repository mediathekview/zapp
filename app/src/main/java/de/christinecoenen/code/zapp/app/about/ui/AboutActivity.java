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
import com.mikepenz.aboutlibraries.util.LibsListenerImpl;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;

public class AboutActivity extends LibsActivity {

	public static Intent getStartIntent(Context context) {
		return new Intent(context, AboutActivity.class);
	}

	private final LibsConfiguration.LibsListener buttonListener = new LibsListenerImpl() {
		@Override
		public void onIconClicked(View v) {
			IntentHelper.openUrl(AboutActivity.this, getString(R.string.app_website_url));
		}

		@Override
		public boolean onExtraClicked(@NonNull View view, Libs.SpecialButton specialButton) {
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
					return super.onExtraClicked(view, specialButton);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = new LibsBuilder()
			.withActivityTitle(getString(R.string.activity_about_title))
			.withAboutDescription(getString(R.string.aboutLibraries_description_text))
			.withFields(R.string.class.getFields())
			.withAutoDetect(true)
			.withLibraries("acra", "commonsio")
			.withListener(buttonListener)
			.intent(this);

		setIntent(intent);

		super.onCreate(savedInstanceState);
	}
}
