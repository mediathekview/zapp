package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions

open class DownloadException(message: String, cause: Throwable? = null)
	: RuntimeException(message, cause)
