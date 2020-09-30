package de.christinecoenen.code.zapp.app.player;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.Objects;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.ZappApplication;
import de.christinecoenen.code.zapp.utils.system.NotificationHelper;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class BackgroundPlayerService extends IntentService implements
	PlayerNotificationManager.MediaDescriptionAdapter, PlayerNotificationManager.NotificationListener {

	private static final String ACTION_START_IN_BACKGROUND = "de.christinecoenen.code.zapp.app.mediathek.controller.action.START_IN_BACKGROUND";

	private final Binder binder = new Binder();

	private Player player;
	private PlayerNotificationManager playerNotificationManager;
	private Intent foregroundActivityIntent;
	private Disposable errorMessageDisposable;
	private boolean isPlaybackInBackground = false;

	public BackgroundPlayerService() {
		super("BackgroundPlayerService");
	}

	/**
	 * Binds this service to the given context.
	 *
	 * @param context
	 * @param serviceConnection will be used to pass a {@link Binder} instance
	 *                          for further communication with this service
	 */
	public static void bind(Context context, ServiceConnection serviceConnection) {
		Intent intent = new Intent(context, BackgroundPlayerService.class);
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private static void startInBackground(Context context) {
		Intent intent = new Intent(context, BackgroundPlayerService.class);
		intent.setAction(ACTION_START_IN_BACKGROUND);
		context.startService(intent);
	}

	/**
	 * This service is only running when
	 * 1. an UI element is currently bound and not in paused state OR
	 * 2. the playback is running in background
	 * <p>
	 * So is is save to aquire locks and release them in {@link #onDestroy()}
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		ZappApplication application = (ZappApplication) getApplicationContext();

		player = new Player(this, application.getPlaybackPositionRepository());
		errorMessageDisposable = player.getErrorResourceId().subscribe(this::onPlayerError);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		handleIntent(intent);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {

	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		onDestroy();
	}

	@Override
	public void onDestroy() {
		movePlaybackToForeground();

		if (player != null) {
			player.destroy();
			player = null;
		}

		errorMessageDisposable.dispose();

		super.onDestroy();
	}

	private void onPlayerError(Integer messageResourceId) {
		if (messageResourceId == null || messageResourceId == -1) {
			return;
		}

		boolean wasPlayingInBackground = isPlaybackInBackground;
		movePlaybackToForeground();

		if (wasPlayingInBackground) {
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				this.foregroundActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			String errorMessage = getString(R.string.error_prefixed_message, getString(messageResourceId));

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_BACKGROUND_PLAYBACK)
				.setContentTitle(player.getCurrentVideoInfo().getTitle())
				.setContentText(errorMessage)
				.setColor(getResources().getColor(R.color.colorPrimaryDark))
				.setSmallIcon(R.drawable.ic_sad_tv)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(messageResourceId)))
				.setOnlyAlertOnce(true)
				.setAutoCancel(true)
				.setContentIntent(pendingIntent);

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Objects.requireNonNull(notificationManager).notify(NotificationHelper.BACKGROUND_PLAYBACK_NOTIFICATION_ID, builder.build());
		}
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.getAction() != null) {
			if (ACTION_START_IN_BACKGROUND.equals(intent.getAction())) {
				handleStartInBackground();
			} else {
				throw new UnsupportedOperationException("Action not supported: " + intent.getAction());
			}
		}
	}

	/**
	 * @see Binder#movePlaybackToBackground()
	 */
	private void movePlaybackToBackground() {
		// start long running task
		BackgroundPlayerService.startInBackground(this);
	}

	/**
	 * @see Binder#movePlaybackToForeground()
	 */
	private void movePlaybackToForeground() {
		isPlaybackInBackground = false;
		stopForeground(true);
		stopSelf();

		if (playerNotificationManager != null) {
			playerNotificationManager.setPlayer(null);
		}
	}

	/**
	 * As soon as somebody starts this service as background player, we create the
	 * player notification. When created this notification will move the service to
	 * foreground to avoid being destroyed by the system.
	 */
	private void handleStartInBackground() {
		isPlaybackInBackground = true;

		playerNotificationManager = new PlayerNotificationManager(this,
			NotificationHelper.CHANNEL_ID_BACKGROUND_PLAYBACK,
			NotificationHelper.BACKGROUND_PLAYBACK_NOTIFICATION_ID,
			this,
			this);
		playerNotificationManager.setSmallIcon(R.drawable.ic_zapp_tv);
		playerNotificationManager.setColor(getResources().getColor(R.color.colorPrimaryDark));
		playerNotificationManager.setPlayer(player.getExoPlayer());
		playerNotificationManager.setMediaSessionToken(player.getMediaSession().getSessionToken());
	}

	@NonNull
	@Override
	public String getCurrentContentTitle(@NonNull com.google.android.exoplayer2.Player player) {
		return this.player.getCurrentVideoInfo().getTitle();
	}

	@Override
	public PendingIntent createCurrentContentIntent(@NonNull com.google.android.exoplayer2.Player player) {
		Timber.i("createCurrentContentIntent: %s", foregroundActivityIntent.getComponent());
		// a notification click will bring us back to the activity that launched it
		return PendingIntent.getActivity(this, 0, foregroundActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public String getCurrentContentText(@NonNull com.google.android.exoplayer2.Player player) {
		return this.player.getCurrentVideoInfo().getSubtitle();
	}

	@Override
	public Bitmap getCurrentLargeIcon(@NonNull com.google.android.exoplayer2.Player player, @NonNull PlayerNotificationManager.BitmapCallback callback) {
		return null;
	}

	@Override
	public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
		if (ongoing) {
			startForeground(notificationId, notification);
		} else {
			stopForeground(false);
		}
	}

	@Override
	public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
		movePlaybackToForeground();
	}

	public class Binder extends android.os.Binder {

		/**
		 * @return Player instance that will live as long as this service is up and running.
		 */
		public Player getPlayer() {
			if (foregroundActivityIntent == null) {
				throw new RuntimeException("Using player without an intent is not allowed. " +
					"Use BackgroundPlayerService.setForegroundActivityIntent.");
			}
			return player;
		}

		/**
		 * @param intent used to bring the calling activity to front
		 *               after finishing background playback
		 */
		public void setForegroundActivityIntent(Intent intent) {
			foregroundActivityIntent = intent;
		}

		/**
		 * Displays a player notification and starts keeping this service alive
		 * in background. Once called the service will resume running until {@link #movePlaybackToForeground()}
		 * is called or the notification is dismissed.
		 */
		public void movePlaybackToBackground() {
			BackgroundPlayerService.this.movePlaybackToBackground();
		}

		/**
		 * Call this once the playback is visible to the user. This will allow this service to
		 * be destroyed as soon as no ui component is bound any more.
		 * This will dismiss the playback notification.
		 */
		public void movePlaybackToForeground() {
			BackgroundPlayerService.this.movePlaybackToForeground();
		}
	}
}
