package de.christinecoenen.code.zapp.app.mediathek.controller.downloads;

import androidx.annotation.NonNull;

import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2core.DownloadBlock;

import java.lang.ref.WeakReference;
import java.util.List;


class SingleDownloadListener implements FetchListener {

	private final int downloadIdentifier;
	private final WeakReference<ISingleDownloadListener> singleDownloadListenerReference;

	SingleDownloadListener(int downloadIdentifier, ISingleDownloadListener singleDownloadListener) {

		this.downloadIdentifier = downloadIdentifier;
		this.singleDownloadListenerReference = new WeakReference<>(singleDownloadListener);
	}

	ISingleDownloadListener getInternalListener() {
		return singleDownloadListenerReference.get();
	}

	@Override
	public void onAdded(@NonNull Download download) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onCancelled(@NonNull Download download) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onCompleted(@NonNull Download download) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onDeleted(@NonNull Download download) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onDownloadBlockUpdated(@NonNull Download download, @NonNull DownloadBlock downloadBlock, int i) {

	}

	@Override
	public void onError(@NonNull Download download, @NonNull Error error, Throwable throwable) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onPaused(@NonNull Download download) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onProgress(@NonNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
		notifyDownloadStatusChanged(download);
		notifyDownloadProgressChanged(download);
	}

	@Override
	public void onQueued(@NonNull Download download, boolean b) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onRemoved(@NonNull Download download) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onResumed(@NonNull Download download) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onStarted(@NonNull Download download, @NonNull List<? extends DownloadBlock> list, int totalBlocks) {
		notifyDownloadStatusChanged(download);
	}

	@Override
	public void onWaitingNetwork(@NonNull Download download) {

	}

	private void notifyDownloadStatusChanged(@NonNull Download download) {
		if (download.getIdentifier() != downloadIdentifier) {
			return;
		}

		ISingleDownloadListener listener = singleDownloadListenerReference.get();
		if (listener != null) {
			listener.onDownloadStatusChanged(download);
		}
	}

	private void notifyDownloadProgressChanged(@NonNull Download download) {
		if (download.getIdentifier() != downloadIdentifier) {
			return;
		}

		ISingleDownloadListener listener = singleDownloadListenerReference.get();
		if (listener != null) {
			listener.onDownloadProgressChanged(download);
		}
	}
}
