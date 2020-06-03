package de.christinecoenen.code.zapp.app.mediathek.controller.downloads;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2core.DownloadBlock;

import java.util.List;

import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;

public class DownloadController implements FetchListener {

	private final Fetch fetch;

	private final ConnectivityManager connectivityManager;
	private final SettingsRepository settingsRepository;
	private final DownloadFileInfoManager downloadFileInfoManager;

	public DownloadController(Context applicationContext) {
		settingsRepository = new SettingsRepository(applicationContext);
		downloadFileInfoManager = new DownloadFileInfoManager(applicationContext, settingsRepository);

		connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);

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
		fetch.addListener(this);
	}

	public void startDownload(MediathekShow show, Quality quality) {
		String downloadUrl = show.getVideoUrl(quality);
		String filePath = downloadFileInfoManager.getDownloadFilePath(show, quality);

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

	public void deleteDownloadsWithDeletedFiles() {
		fetch.getDownloadsWithStatus(Status.COMPLETED, downloads -> {
			for (Download download : downloads) {
				if (downloadFileInfoManager.shouldDeleteDownload(download)) {
					fetch.remove(download.getId());
				}
			}
		});
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

	@Override
	public void onAdded(@NonNull Download download) {

	}

	@Override
	public void onCancelled(@NonNull Download download) {

	}

	@Override
	public void onCompleted(@NonNull Download download) {
		downloadFileInfoManager.updateDownloadFileInMediaCollection(download);
	}

	@Override
	public void onDeleted(@NonNull Download download) {
		downloadFileInfoManager.updateDownloadFileInMediaCollection(download);
	}

	@Override
	public void onDownloadBlockUpdated(@NonNull Download download, @NonNull DownloadBlock downloadBlock, int i) {

	}

	@Override
	public void onError(@NonNull Download download, @NonNull Error error, Throwable throwable) {

	}

	@Override
	public void onPaused(@NonNull Download download) {

	}

	@Override
	public void onProgress(@NonNull Download download, long l, long l1) {

	}

	@Override
	public void onQueued(@NonNull Download download, boolean b) {

	}

	@Override
	public void onRemoved(@NonNull Download download) {

	}

	@Override
	public void onResumed(@NonNull Download download) {

	}

	@Override
	public void onStarted(@NonNull Download download, @NonNull List<? extends DownloadBlock> list, int i) {

	}

	@Override
	public void onWaitingNetwork(@NonNull Download download) {

	}
}
