package de.christinecoenen.code.zapp.mediathek.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.MediathekShow;

public class MediathekDetailActivity extends AppCompatActivity {

	private static final String EXTRA_SHOW = "de.christinecoenen.code.zapp.EXTRA_SHOW";

	public static Intent getStartIntent(Context context, MediathekShow show) {
		Intent intent = new Intent(context, MediathekDetailActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra(EXTRA_SHOW, show);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mediathek_detail);
		ButterKnife.bind(this);

		MediathekShow show = (MediathekShow) getIntent().getExtras().getSerializable(EXTRA_SHOW);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
				.add(R.id.container, MediathekDetailFragment.getInstance(show), "MediathekDetailFragment")
				.commit();
		}
	}
}
