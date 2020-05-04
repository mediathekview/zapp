package de.christinecoenen.code.zapp.app.mediathek.controller.exceptions;

public class DownloadException extends RuntimeException {

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

}
