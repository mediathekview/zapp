package de.christinecoenen.code.zapp.app.mediathek.controller.downloads;

import androidx.annotation.NonNull;

import com.tonyodev.fetch2.Download;

public interface ISingleDownloadListener {

	void onDownloadProgressChanged(@NonNull Download download);

	void onDownloadStatusChanged(@NonNull Download download);
	
}
