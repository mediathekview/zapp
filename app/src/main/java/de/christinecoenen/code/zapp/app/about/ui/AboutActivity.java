package de.christinecoenen.code.zapp.app.about.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.ui.LibsActivity;

import de.christinecoenen.code.zapp.R;

public class AboutActivity extends LibsActivity {

	public static Intent getStartIntent(Context context) {
		return new Intent(context, AboutActivity.class);
	}

	private final LibsConfiguration.LibsListener buttonListener = new LibsConfiguration.LibsListenerImpl() {
		@Override
		public boolean onExtraClicked(View v, Libs.SpecialButton specialButton) {
			switch (specialButton) {
				case SPECIAL1:
					startActivity(FaqActivity.getStartIntent(AboutActivity.this));
					return true;
				case SPECIAL2:
					startActivity(ChangelogActivity.getStartIntent(AboutActivity.this));
					return true;
				default:
					return super.onExtraClicked(v, specialButton);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Intent intent = new LibsBuilder()
			.withActivityTitle(getString(R.string.activity_about_title))
			.withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
			.withFields(R.string.class.getFields())
			.withAutoDetect(true)
			.withLibraries("acra", "commonsio")
			.withListener(buttonListener)
			.intent(this);

		setIntent(intent);

		super.onCreate(savedInstanceState);
	}
}
