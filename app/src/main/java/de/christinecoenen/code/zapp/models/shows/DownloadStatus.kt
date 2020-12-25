package de.christinecoenen.code.zapp.models.shows

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
