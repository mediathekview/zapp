package de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions;

public class DownloadException extends RuntimeException {

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

}
