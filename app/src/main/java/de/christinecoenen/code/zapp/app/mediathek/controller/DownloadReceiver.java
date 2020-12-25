package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.christinecoenen.code.zapp.app.ZappApplication;
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow;
import de.christinecoenen.code.zapp.repositories.MediathekRepository;
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;


public class DownloadReceiver extends BroadcastReceiver {

	private static final String ACTION_NOTIFICATION_CLICKED = "de.christinecoenen.code.zapp.NOTIFICATION_CLICKED";
	private static final String EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID";

	public static Intent getNotificationClickedIntent(Context context, int downloadId) {
		Intent intent = new Intent(context, DownloadReceiver.class);
		intent.setAction(ACTION_NOTIFICATION_CLICKED);
		intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
		return intent;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null) {
			return;
		}

		String action = intent.getAction();

		if (ACTION_NOTIFICATION_CLICKED.equals(action)) {

			ZappApplication application = (ZappApplication) context.getApplicationContext();
			MediathekRepository mediathekRepository = application.getMediathekRepository();
			int downloadId = intent.getIntExtra(EXTRA_DOWNLOAD_ID, 0);

			Disposable loadShowDisposable = mediathekRepository
				.getPersistedShowByDownloadId(downloadId)
				.firstElement()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(persistedMediathekShow -> onShowLoaded(context, persistedMediathekShow), Timber::e);
		}
	}

	private void onShowLoaded(Context context, PersistedMediathekShow persistedMediathekShow) {
		// launch MediathekDetailActivity
		Intent detailIntent = MediathekDetailActivity.getStartIntent(context, persistedMediathekShow.getMediathekShow());
		detailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(detailIntent);
	}
}
