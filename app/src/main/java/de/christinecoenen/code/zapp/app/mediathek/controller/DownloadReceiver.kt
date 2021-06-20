package de.christinecoenen.code.zapp.app.mediathek.controller

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class DownloadReceiver : BroadcastReceiver(), KoinComponent {

	companion object {

		private const val ACTION_NOTIFICATION_CLICKED = "de.christinecoenen.code.zapp.NOTIFICATION_CLICKED"
		private const val EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID"

		fun getNotificationClickedIntent(context: Context?, downloadId: Int): Intent {
			return Intent(context, DownloadReceiver::class.java).apply {
				action = ACTION_NOTIFICATION_CLICKED
				putExtra(EXTRA_DOWNLOAD_ID, downloadId)
			}
		}

	}

	private val mediathekRepository: MediathekRepository by inject()

	@SuppressLint("CheckResult")
	override fun onReceive(context: Context, intent: Intent) {
		if (ACTION_NOTIFICATION_CLICKED != intent.action) {
			return
		}

		val downloadId = intent.getIntExtra(EXTRA_DOWNLOAD_ID, 0)

		mediathekRepository
			.getPersistedShowByDownloadId(downloadId)
			.firstElement()
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({ show -> onShowLoaded(context, show) }, Timber::e)
	}

	private fun onShowLoaded(context: Context, persistedMediathekShow: PersistedMediathekShow) {
		// launch MediathekDetailActivity
		val detailIntent = MediathekDetailActivity
			.getStartIntent(context, persistedMediathekShow.mediathekShow)
			.apply {
				addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			}

		context.startActivity(detailIntent)
	}
}
