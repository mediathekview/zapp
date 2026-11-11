package de.christinecoenen.code.zapp.app.personal.series

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.christinecoenen.code.zapp.repositories.ShowCollectionRepository
import de.christinecoenen.code.zapp.utils.system.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Periodically checks all collections for new shows and notifies the user about them.
 */
class CollectionUpdateWorker(
	appContext: Context,
	workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

	companion object {
		/**
		 * Notifications are posted with a tag, so their ids cannot collide with other
		 * notifications of the app.
		 */
		const val NOTIFICATION_TAG = "collection_update"
	}

	private val showCollectionRepository: ShowCollectionRepository by inject()

	private val notificationManager = NotificationManagerCompat.from(applicationContext)

	@SuppressLint("MissingPermission")
	override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
		if (!NotificationHelper.hasNotificationPermissionGranted(applicationContext)) {
			return@withContext Result.success()
		}

		try {
			showCollectionRepository.findCollectionsWithNewShows().forEach { update ->
				notificationManager.notify(
					NOTIFICATION_TAG,
					update.collection.id,
					CollectionUpdateNotification(
						applicationContext,
						update.collection,
						update.newShowCount
					).build()
				)
			}

			Result.success()
		} catch (e: Exception) {
			Timber.e(e, "Could not check collections for new shows")
			Result.retry()
		}
	}
}
