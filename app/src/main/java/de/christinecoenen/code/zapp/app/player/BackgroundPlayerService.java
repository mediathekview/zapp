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

import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.utils.system.NotificationHelper;
import io.reactivex.disposables.Disposable;

public class BackgroundPlayerService extends IntentService implements
	PlayerNotificationManager.MediaDescriptionAdapter, PlayerNotificationManager.NotificationListener {

	private static final String ACTION_START_IN_BACKGROUND = "de.christinecoenen.code.zapp.app.mediathek.controller.action.START_IN_BACKGROUND";
	private static final String ACTION_NOTIFICATION_CLICKED = "de.christinecoenen.code.zapp.app.mediathek.controller.action.NOTIFICATION_CLICKED";
	private static final String EXTRA_FOREGROUND_INTENT = "EXTRA_FOREGROUND_INTENT";

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
	 * @param serviceConnection        will be used to pass a {@link Binder} instance
	 *                                 for further communication with this service
	 * @param foregroundActivityIntent used to bring the calling activity to front
	 *                                 after finishing background playback
	 */
	public static void bind(Context context, ServiceConnection serviceConnection, Intent foregroundActivityIntent) {
		Intent intent = new Intent(context, BackgroundPlayerService.class);
		intent.putExtra(EXTRA_FOREGROUND_INTENT, foregroundActivityIntent);
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private static void startInBackground(Context context) {
		Intent intent = new Intent(context, BackgroundPlayerService.class);
		intent.setAction(ACTION_START_IN_BACKGROUND);
		context.startService(intent);
	}

	private static Intent getNotificationClickedIntent(Context context) {
		Intent intent = new Intent(context, BackgroundPlayerService.class);
		intent.setAction(ACTION_NOTIFICATION_CLICKED);
		return intent;
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

		player = new Player(this);
		errorMessageDisposable = player.getErrorResourceId().subscribe(this::onPlayerError);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		foregroundActivityIntent = intent.getParcelableExtra(EXTRA_FOREGROUND_INTENT);
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
	public void onDestroy() {
		movePlaybackToForeground();

		if (player != null) {
			player.destroy();
			player = null;
		}

		errorMessageDisposable.dispose();

		super.onDestroy();
	}

	private void onPlayerError(int messageResourceId) {
		boolean wasPlayingInBackground = isPlaybackInBackground;
		movePlaybackToForeground();

		if (wasPlayingInBackground) {
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				this.foregroundActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			String errorMessage = getString(R.string.error_prefixed_message, getString(messageResourceId));

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationHelper.BACKGROUND_PLAYBACK_CHANNEL_ID)
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
			switch (intent.getAction()) {
				case ACTION_NOTIFICATION_CLICKED:
					handleNotificationClicked();
					break;
				case ACTION_START_IN_BACKGROUND:
					handleStartInBackground();
					break;
				default:
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
			NotificationHelper.BACKGROUND_PLAYBACK_CHANNEL_ID,
			NotificationHelper.BACKGROUND_PLAYBACK_NOTIFICATION_ID,
			this);
		playerNotificationManager.setOngoing(false);
		playerNotificationManager.setPlayer(player.getExoPlayer());
		playerNotificationManager.setNotificationListener(this);
		playerNotificationManager.setSmallIcon(R.drawable.ic_zapp_tv);
		playerNotificationManager.setColor(getResources().getColor(R.color.colorPrimaryDark));
		playerNotificationManager.setMediaSessionToken(player.getMediaSession().getSessionToken());
	}

	private void handleNotificationClicked() {
		startActivity(foregroundActivityIntent);
	}

	@Override
	public String getCurrentContentTitle(com.google.android.exoplayer2.Player player) {
		return this.player.getCurrentVideoInfo().getTitle();
	}

	@Override
	public PendingIntent createCurrentContentIntent(com.google.android.exoplayer2.Player player) {
		// a notification click will bring us back to this service
		Intent intent = BackgroundPlayerService.getNotificationClickedIntent(this);
		return PendingIntent.getService(BackgroundPlayerService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public String getCurrentContentText(com.google.android.exoplayer2.Player player) {
		return this.player.getCurrentVideoInfo().getSubtitle();
	}

	@Override
	public Bitmap getCurrentLargeIcon(com.google.android.exoplayer2.Player player, PlayerNotificationManager.BitmapCallback callback) {
		return null;
	}

	@Override
	public void onNotificationStarted(int notificationId, Notification notification) {
		startForeground(notificationId, notification);
	}

	@Override
	public void onNotificationCancelled(int notificationId) {
		movePlaybackToForeground();
	}

	public class Binder extends android.os.Binder {

		/**
		 * @return Player instance that will live as long as this service is up and running.
		 */
		public Player getPlayer() {
			return player;
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
