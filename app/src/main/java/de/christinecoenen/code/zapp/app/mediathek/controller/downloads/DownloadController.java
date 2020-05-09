package de.christinecoenen.code.zapp.app.mediathek.controller.downloads;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;

import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;

public class DownloadController {

	private final Fetch fetch;

	private final DownloadManager downloadManager;
	private final ConnectivityManager connectivityManager;
	private final SettingsRepository settingsRepository;

	public DownloadController(Context applicationContext) {
		downloadManager = (DownloadManager) applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
		connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);

		settingsRepository = new SettingsRepository(applicationContext);

		FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(applicationContext)
			.setNotificationManager(new ZappNotificationManager(applicationContext) {
				@NonNull
				@Override
				public Fetch getFetchInstanceForNamespace(@NonNull String namespace) {
					return fetch;
				}
			})
			.enableRetryOnNetworkGain(false)
			.setAutoRetryMaxAttempts(0)
			.build();

		fetch = Fetch.Impl.getInstance(fetchConfiguration);
	}

	public long startDownload(MediathekShow show, Quality quality) {
		String downloadUrl = show.getVideoUrl(quality);
		String fileName = show.getDownloadFileName(quality);
		String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath()
			+ "/zapp/" + fileName;

		Request request;
		try {
			// create request for android download manager
			request = new Request(downloadUrl, filePath);
		} catch (Exception e) {
			throw new DownloadException("Constructing download request failed.", e);
		}

		return enqueueDownload(request);
	}

	public void stopDownload(long downloadId) {
		downloadManager.remove(downloadId);
	}

	private long enqueueDownload(Request request) {
		NetworkType networkType = settingsRepository.getDownloadOverWifiOnly() ?
			NetworkType.WIFI_ONLY : NetworkType.ALL;
		request.setNetworkType(networkType);

		if (downloadManager == null || connectivityManager == null) {
			throw new DownloadException("No download manager available.");
		} else if (settingsRepository.getDownloadOverWifiOnly() && connectivityManager.isActiveNetworkMetered()) {
			throw new WrongNetworkConditionException("Download over metered networks prohibited.");
		}

		fetch.enqueue(request, null, null);

		return request.getId();
	}
}
