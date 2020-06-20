package de.christinecoenen.code.zapp.app.mediathek.model

enum class DownloadStatus {
	NONE,
	QUEUED,
	DOWNLOADING,
	PAUSED,
	COMPLETED,
	CANCELLED,
	FAILED,
	REMOVED,
	DELETED,
	ADDED;
}
