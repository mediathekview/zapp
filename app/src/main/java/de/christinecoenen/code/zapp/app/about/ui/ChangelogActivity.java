package de.christinecoenen.code.zapp.app.about.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import ru.noties.markwon.Markwon;
import timber.log.Timber;

public class ChangelogActivity extends AppCompatActivity {

	@BindView(R.id.txt_changelog)
	protected TextView changelogText;


	public static Intent getStartIntent(Context context) {
		return new Intent(context, ChangelogActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_changelog);
		ButterKnife.bind(this, this);

		try (InputStream inputStream = getResources().openRawResource(R.raw.changelog)) {
			String markdown = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
			Markwon.setMarkdown(changelogText, markdown);
		} catch (IOException e) {
			Timber.e(e);
		}
	}
}
