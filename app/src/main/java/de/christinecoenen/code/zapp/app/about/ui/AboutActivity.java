package de.christinecoenen.code.zapp.app.about.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;

public class AboutActivity extends AppCompatActivity {

	private static final String TAG = AboutActivity.class.getSimpleName();

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

		try {
			versionText.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, "could not retreive version name");
		}
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
