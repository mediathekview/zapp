package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.models.shows.MediathekShow;

public class MediathekDetailActivity extends AppCompatActivity {

	private static final String EXTRA_SHOW = "de.christinecoenen.code.zapp.EXTRA_SHOW";

	public static Intent getStartIntent(Context context, MediathekShow show) {
		Intent intent = new Intent(context, MediathekDetailActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra(EXTRA_SHOW, show);
		return intent;
	}

	private MediathekShow show;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mediathek_detail);

		//noinspection ConstantConditions
		show = (MediathekShow) getIntent().getExtras().getSerializable(EXTRA_SHOW);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
				.add(R.id.container, MediathekDetailFragment.getInstance(show), "MediathekDetailFragment")
				.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_mediathek_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				startActivity(Intent.createChooser(show.getShareIntentPlain(), getString(R.string.action_share)));
				return true;
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
