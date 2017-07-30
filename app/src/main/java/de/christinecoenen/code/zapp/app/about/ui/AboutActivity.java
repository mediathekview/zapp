package de.christinecoenen.code.zapp.app.about.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;
import timber.log.Timber;

public class AboutActivity extends AppCompatActivity {

	public static Intent getStartIntent(Context context) {
		return new Intent(context, AboutActivity.class);
	}

	@BindView(R.id.text_version)
	TextView versionText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);
		ButterKnife.bind(this);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		try {
			versionText.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (PackageManager.NameNotFoundException e) {
			Timber.w("could not retreive version name");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@OnClick(R.id.button_faq)
	public void onFaqButtonClick() {
		startActivity(FaqActivity.getStartIntent(this));
	}

	@OnClick(R.id.button_changelog)
	public void onChangelogButtonClick() {
		startActivity(ChangelogActivity.getStartIntent(this));
	}
}
