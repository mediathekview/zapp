package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.christinecoenen.code.zapp.app.ZappApplication;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository;
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.tonyodev.fetch2.FetchIntent.EXTRA_ACTION_TYPE;


public class DownloadReceiver extends BroadcastReceiver {

	public static final int ACTION_NOTIFICATION_CLICKED = 42;

	private static final String EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID";

	public static Intent getNotificationClickedIntent(String targetAction, int downloadId) {
		Intent intent = new Intent(targetAction);
		intent.putExtra(EXTRA_ACTION_TYPE, ACTION_NOTIFICATION_CLICKED);
		intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
		return intent;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null) {
			return;
		}

		int actionType = intent.getIntExtra(EXTRA_ACTION_TYPE, ACTION_NOTIFICATION_CLICKED);

		if (actionType == ACTION_NOTIFICATION_CLICKED) {

			ZappApplication application = (ZappApplication) context.getApplicationContext();
			MediathekRepository mediathekRepository = application.getMediathekRepository();
			int downloadId = intent.getIntExtra(EXTRA_DOWNLOAD_ID, 0);

			//noinspection unused
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
