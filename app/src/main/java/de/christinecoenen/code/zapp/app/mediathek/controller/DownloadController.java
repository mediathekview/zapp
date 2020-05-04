package de.christinecoenen.code.zapp.app.mediathek.controller;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;

import de.christinecoenen.code.zapp.app.mediathek.controller.exceptions.DownloadException;
import de.christinecoenen.code.zapp.app.mediathek.controller.exceptions.WrongNetworkConditionException;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;

public class DownloadController {

	private final DownloadManager downloadManager;
	private final ConnectivityManager connectivityManager;
	private final SettingsRepository settingsRepository;

	public DownloadController(Context applicationContext) {
		downloadManager = (DownloadManager) applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
		connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);

		settingsRepository = new SettingsRepository(applicationContext);
	}

	public long startDownload(MediathekShow show, Quality quality) {
		String downloadUrl = show.getVideoUrl(quality);
		Uri downloadUri = Uri.parse(downloadUrl);
		String fileName = show.getDownloadFileName(quality);

		DownloadManager.Request request;
		try {
			// create request for android download manager
			request = new DownloadManager.Request(downloadUri);
		} catch (Exception e) {
			throw new DownloadException("Constructing download request failed.", e);
		}

		return enqueueDownload(show, request, fileName);
	}

	public void stopDownload(long downloadId) {
		downloadManager.remove(downloadId);
	}

	private long enqueueDownload(MediathekShow show, DownloadManager.Request request, String downloadFileName) {
		// setting title and directory of request
		request.setTitle(show.getTitle());
		request.allowScanningByMediaScanner();
		request.setVisibleInDownloadsUi(true);
		request.setAllowedOverMetered(!settingsRepository.getDownloadOverWifiOnly());
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "zapp/" + downloadFileName);

		if (downloadManager == null || connectivityManager == null) {
			throw new DownloadException("No download manager available.");
		} else if (settingsRepository.getDownloadOverWifiOnly() && connectivityManager.isActiveNetworkMetered()) {
			throw new WrongNetworkConditionException("Download over metered networks prohibited.");
		}

		return downloadManager.enqueue(request);
	}
}
