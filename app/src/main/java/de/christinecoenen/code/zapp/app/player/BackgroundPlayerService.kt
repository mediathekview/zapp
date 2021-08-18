package de.christinecoenen.code.zapp.app.player

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.utils.system.NotificationHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class BackgroundPlayerService : IntentService("BackgroundPlayerService"),
	MediaDescriptionAdapter,
	PlayerNotificationManager.NotificationListener,
	LifecycleOwner {


	companion object {

		private const val ACTION_START_IN_BACKGROUND =
			"de.christinecoenen.code.zapp.app.mediathek.controller.action.START_IN_BACKGROUND"

		/**
		 * Binds this service to the given context.
		 *
		 * @param context
		 * @param serviceConnection will be used to pass a [Binder] instance
		 * for further communication with this service
		 */
		@JvmStatic
		fun bind(context: Context, serviceConnection: ServiceConnection?) {
			val intent = Intent(context, BackgroundPlayerService::class.java)
			context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
		}

		private fun startInBackground(context: Context) {
			val intent = Intent(context, BackgroundPlayerService::class.java)
			intent.action = ACTION_START_IN_BACKGROUND
			context.startService(intent)
		}
	}

	private val player: Player by inject()

	private val binder = Binder()

	private val lifecycleDispatcher: ServiceLifecycleDispatcher = ServiceLifecycleDispatcher(this)

	private var playerNotificationManager: PlayerNotificationManager? = null
	private var foregroundActivityIntent: Intent? = null
	private var isPlaybackInBackground = false

	/**
	 * This service is only running when
	 * 1. an UI element is currently bound and not in paused state OR
	 * 2. the playback is running in background
	 *
	 * So is is save to aquire locks and release them in [.onDestroy]
	 */
	override fun onCreate() {
		lifecycleDispatcher.onServicePreSuperOnCreate()
		super.onCreate()

		lifecycleScope.launchWhenCreated {
			player.errorResourceId.collect(::onPlayerError)
		}
	}

	override fun onBind(intent: Intent): IBinder {
		lifecycleDispatcher.onServicePreSuperOnBind()
		super.onBind(intent)

		return binder
	}

	override fun onStart(intent: Intent?, startId: Int) {
		lifecycleDispatcher.onServicePreSuperOnStart()
		super.onStart(intent, startId)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		handleIntent(intent)
		return Service.START_STICKY
	}

	override fun onHandleIntent(intent: Intent?) {}

	override fun getLifecycle(): Lifecycle {
		return lifecycleDispatcher.lifecycle
	}

	override fun onTaskRemoved(rootIntent: Intent) {
		super.onTaskRemoved(rootIntent)
		onDestroy()
	}

	override fun onDestroy() {
		movePlaybackToForeground()

		GlobalScope.launch {
			player.destroy()
		}

		lifecycleDispatcher.onServicePreSuperOnDestroy()
		super.onDestroy()
	}

	private fun onPlayerError(messageResourceId: Int?) {
		if (messageResourceId == null || messageResourceId == -1) {
			return
		}

		val wasPlayingInBackground = isPlaybackInBackground

		movePlaybackToForeground()

		if (wasPlayingInBackground) {
			val pendingIntent = PendingIntent.getActivity(
				this, 0,
				foregroundActivityIntent,
				PendingIntent.FLAG_UPDATE_CURRENT
			)

			val errorMessage =
				getString(R.string.error_prefixed_message, getString(messageResourceId))

			val builder =
				NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_BACKGROUND_PLAYBACK)
					.setContentTitle(player.currentVideoInfo!!.title)
					.setContentText(errorMessage)
					.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
					.setSmallIcon(R.drawable.ic_sad_tv)
					.setStyle(
						NotificationCompat.BigTextStyle().bigText(getString(messageResourceId))
					)
					.setOnlyAlertOnce(true)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent)

			val notificationManager =
				getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.notify(
				NotificationHelper.BACKGROUND_PLAYBACK_NOTIFICATION_ID,
				builder.build()
			)
		}
	}

	private fun handleIntent(intent: Intent?) {
		if (intent == null || intent.action == null) {
			return
		}

		if (ACTION_START_IN_BACKGROUND == intent.action) {
			handleStartInBackground()
		} else {
			throw UnsupportedOperationException("Action not supported: " + intent.action)
		}
	}

	/**
	 * @see Binder.movePlaybackToBackground
	 */
	private fun movePlaybackToBackground() {
		// start long running task
		startInBackground(this)
	}

	/**
	 * @see Binder.movePlaybackToForeground
	 */
	private fun movePlaybackToForeground() {
		isPlaybackInBackground = false

		stopForeground(true)
		stopSelf()

		playerNotificationManager?.apply {
			setPlayer(null)
		}
	}

	/**
	 * As soon as somebody starts this service as background player, we create the
	 * player notification. When created this notification will move the service to
	 * foreground to avoid being destroyed by the system.
	 */
	private fun handleStartInBackground() {
		isPlaybackInBackground = true

		playerNotificationManager = PlayerNotificationManager
			.Builder(
				this,
				NotificationHelper.BACKGROUND_PLAYBACK_NOTIFICATION_ID,
				NotificationHelper.CHANNEL_ID_BACKGROUND_PLAYBACK
			)
			.build()
			.also {
				it.setSmallIcon(R.drawable.ic_zapp_tv)
				it.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
				it.setPlayer(player.exoPlayer)
				it.setMediaSessionToken(player.mediaSession.sessionToken)
			}
	}

	override fun getCurrentContentTitle(player: com.google.android.exoplayer2.Player): String {
		return this.player.currentVideoInfo!!.title
	}

	override fun createCurrentContentIntent(player: com.google.android.exoplayer2.Player): PendingIntent? {
		Timber.i("createCurrentContentIntent: %s", foregroundActivityIntent!!.component)

		// a notification click will bring us back to the activity that launched it
		return PendingIntent.getActivity(
			this,
			0,
			foregroundActivityIntent,
			PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	override fun getCurrentContentText(player: com.google.android.exoplayer2.Player): String? =
		this.player.currentVideoInfo!!.subtitle

	override fun getCurrentLargeIcon(
		player: com.google.android.exoplayer2.Player,
		callback: BitmapCallback
	): Bitmap? = null

	override fun onNotificationPosted(
		notificationId: Int,
		notification: Notification,
		ongoing: Boolean
	) {
		if (ongoing) {
			startForeground(notificationId, notification)
		} else {
			stopForeground(false)
		}
	}

	override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) =
		movePlaybackToForeground()

	inner class Binder : android.os.Binder() {
		/**
		 * @return Player instance that will live as long as this service is up and running.
		 */
		fun getPlayer(): Player {
			if (foregroundActivityIntent == null) {
				throw RuntimeException(
					"Using player without an intent is not allowed. " +
						"Use BackgroundPlayerService.setForegroundActivityIntent."
				)
			}
			return player
		}

		/**
		 * @param intent used to bring the calling activity to front
		 * after finishing background playback
		 */
		fun setForegroundActivityIntent(intent: Intent?) {
			foregroundActivityIntent = intent
		}

		/**
		 * Displays a player notification and starts keeping this service alive
		 * in background. Once called the service will resume running until [.movePlaybackToForeground]
		 * is called or the notification is dismissed.
		 */
		fun movePlaybackToBackground() = this@BackgroundPlayerService.movePlaybackToBackground()

		/**
		 * Call this once the playback is visible to the user. This will allow this service to
		 * be destroyed as soon as no ui component is bound any more.
		 * This will dismiss the playback notification.
		 */
		fun movePlaybackToForeground() = this@BackgroundPlayerService.movePlaybackToForeground()
	}
}
