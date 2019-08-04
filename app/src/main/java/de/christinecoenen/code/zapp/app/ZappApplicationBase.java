package de.christinecoenen.code.zapp.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;
import de.christinecoenen.code.zapp.repositories.ChannelRepository;
import de.christinecoenen.code.zapp.utils.system.NotificationHelper;


@SuppressWarnings("WeakerAccess")
public abstract class ZappApplicationBase extends Application {

	private ChannelRepository channelRepository;

	public ChannelRepository getChannelRepository() {
		return channelRepository;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpLogging();

		NotificationHelper.createBackgroundPlaybackChannel(this);

		SettingsRepository settingsRepository = new SettingsRepository(this);
		AppCompatDelegate.setDefaultNightMode(settingsRepository.getUiMode());

		channelRepository = new ChannelRepository(this);
	}

	protected abstract void setUpLogging();
}
