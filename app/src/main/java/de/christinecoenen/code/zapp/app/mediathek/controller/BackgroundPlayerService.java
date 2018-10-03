package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekPlayerActivity;
import de.christinecoenen.code.zapp.utils.system.NotificationHelper;
import de.christinecoenen.code.zapp.utils.video.VideoBufferingHandler;
import de.christinecoenen.code.zapp.utils.video.VideoErrorHandler;
import timber.log.Timber;

public class BackgroundPlayerService extends IntentService implements
	PlayerNotificationManager.MediaDescriptionAdapter, PlayerNotificationManager.NotificationListener,
	VideoBufferingHandler.IVideoBufferingListener, VideoErrorHandler.IVideoErrorListener {

	private static final String ACTION_START = "de.christinecoenen.code.zapp.app.mediathek.controller.action.START";
	private static final String ACTION_STOP = "de.christinecoenen.code.zapp.app.mediathek.controller.action.STOP";

	private static final String EXTRA_SHOW = "de.christinecoenen.code.zapp.app.mediathek.controller.extra.SHOW";

	private Player player;
	private PlayerNotificationManager playerNotificationManager;
	private MediathekShow show;
	private PowerManager.WakeLock wakeLock;
	private WifiManager.WifiLock wifiLock;

	public BackgroundPlayerService() {
		super("BackgroundPlayerService");
	}

	public static void startActionStart(Context context, MediathekShow show) {
		Intent intent = new Intent(context, BackgroundPlayerService.class);
		intent.setAction(ACTION_START);
		intent.putExtra(EXTRA_SHOW, show);
		context.startService(intent);
	}

	public static void startActionStop(Context context) {
		Intent intent = new Intent(context, BackgroundPlayerService.class);
		intent.setAction(ACTION_STOP);
		context.startService(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		if (powerManager != null) {
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Zapp::BackgroundPlayerService");
		}

		WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		if (wifiManager != null) {
			wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Zapp::BackgroundPlayerService");
		}
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
		if (player != null) {
			playerNotificationManager.setPlayer(null);
			player.destroy();
			player = null;
		}

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		if (wifiLock.isHeld()) {
			wifiLock.release();
		}

		super.onDestroy();
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.getAction() != null) {
			switch (intent.getAction()) {
				case ACTION_START:
					final MediathekShow show = (MediathekShow) intent.getSerializableExtra(EXTRA_SHOW);
					handleActionStart(show);
					break;
				case ACTION_STOP:
					handleActionStop();
					break;
			}
		}
	}

	private void handleActionStart(MediathekShow show) {
		this.show = show;

		player = new Player(this, show, this, this);
		// TODO: seek to last position
		player.resume();

		playerNotificationManager = new PlayerNotificationManager(this,
			NotificationHelper.BACKGROUND_PLAYBACK_CHANNEL_ID,
			NotificationHelper.BACKGROUND_PLAYBACK_NOTIFICATION_ID,
			this);
		playerNotificationManager.setOngoing(false);
		playerNotificationManager.setPlayer(player.getExoPlayer());
		playerNotificationManager.setNotificationListener(this);
		playerNotificationManager.setSmallIcon(R.drawable.ic_zapp_tv);
		playerNotificationManager.setColor(getResources().getColor(R.color.colorPrimaryDark));

		wakeLock.acquire(TimeUnit.MINUTES.toMillis(120));
		wifiLock.acquire();
	}

	private void handleActionStop() {
		stopForeground(true);
		stopSelf();
	}

	@Override
	public String getCurrentContentTitle(com.google.android.exoplayer2.Player player) {
		return show.getTitle();
	}

	@Override
	public PendingIntent createCurrentContentIntent(com.google.android.exoplayer2.Player player) {
		// TODO: pass current video time
		Intent intent = MediathekPlayerActivity.getStartIntent(BackgroundPlayerService.this, show);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return PendingIntent.getActivity(BackgroundPlayerService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public String getCurrentContentText(com.google.android.exoplayer2.Player player) {
		return show.getTopic();
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
		handleActionStop();
	}

	@Override
	public void onBufferingStarted() {

	}

	@Override
	public void onBufferingEnded() {

	}

	@Override
	public void onVideoEnded() {
		handleActionStop();
	}

	@Override
	public void onVideoError(int messageResourceId) {
		Timber.w("video playback error: %s", getString(messageResourceId));
		// TODO: display video error to user
	}

	@Override
	public void onVideoErrorInvalid() {

	}
}
