package de.christinecoenen.code.zapp.app.mediathek.controller.downloads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;

import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;

public class DownloadController {

	private final Fetch fetch;

	private final ConnectivityManager connectivityManager;
	private final SettingsRepository settingsRepository;

	public DownloadController(Context applicationContext) {
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

	public void startDownload(MediathekShow show, Quality quality) {
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

		enqueueDownload(show, request);
	}

	public void stopDownload(String showId) {
		fetch.getDownloadsByRequestIdentifier(showId.hashCode(), result -> {
			if (!result.isEmpty()) {
				fetch.cancel(result.get(0).getId());
			}
		});
	}

	public void deleteDownload(String showId) {
		fetch.getDownloadsByRequestIdentifier(showId.hashCode(), result -> {
			if (!result.isEmpty()) {
				fetch.delete(result.get(0).getId());
			}
		});
	}

	public void addSigleDownloadListener(String showId, ISingleDownloadListener listener) {
		FetchListener fetchListener = new SingleDownloadListener(showId.hashCode(), listener);
		fetch.addListener(fetchListener, true);
	}

	public void removeSigleDownloadListener(ISingleDownloadListener listener) {
		for (FetchListener fetchListener : fetch.getListenerSet()) {
			if (fetchListener instanceof SingleDownloadListener) {
				SingleDownloadListener singleDownloadListener = (SingleDownloadListener) fetchListener;
				ISingleDownloadListener internalListener = singleDownloadListener.getInternalListener();
				if (internalListener == null || internalListener == listener) {
					fetch.removeListener(fetchListener);
				}
			}
		}
	}

	private void enqueueDownload(MediathekShow show, Request request) {
		NetworkType networkType = settingsRepository.getDownloadOverWifiOnly() ?
			NetworkType.WIFI_ONLY : NetworkType.ALL;
		request.setNetworkType(networkType);
		request.setIdentifier(show.getId().hashCode());

		if (settingsRepository.getDownloadOverWifiOnly() && connectivityManager.isActiveNetworkMetered()) {
			throw new WrongNetworkConditionException("Download over metered networks prohibited.");
		}

		fetch.enqueue(request, null, null);
	}
}
