package de.christinecoenen.code.zapp.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController;
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository;
import de.christinecoenen.code.zapp.app.mediathek.repository.persistence.MediathekDatabase;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.repositories.ChannelRepository;
import de.christinecoenen.code.zapp.utils.system.NotificationHelper;


@SuppressWarnings("WeakerAccess")
public abstract class ZappApplicationBase extends Application {

	private ChannelRepository channelRepository;
	private MediathekRepository mediathekRepository;
	private DownloadController downloadController;


	public ChannelRepository getChannelRepository() {
		return channelRepository;
	}

	public MediathekRepository getMediathekRepository() {
		return mediathekRepository;
	}

	public DownloadController getDownloadController() {
		return downloadController;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpLogging();

		NotificationHelper.createBackgroundPlaybackChannel(this);

		SettingsRepository settingsRepository = new SettingsRepository(this);
		AppCompatDelegate.setDefaultNightMode(settingsRepository.getUiMode());

		channelRepository = new ChannelRepository(this);

		MediathekDatabase mediathekDatabase = MediathekDatabase.Companion.getInstance(this);

		mediathekRepository = new MediathekRepository(mediathekDatabase);
		downloadController = new DownloadController(this, mediathekRepository);
	}

	protected abstract void setUpLogging();
}
