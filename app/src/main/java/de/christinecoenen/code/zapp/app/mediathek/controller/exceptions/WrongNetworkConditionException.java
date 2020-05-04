package de.christinecoenen.code.zapp.app.mediathek.controller.exceptions;

public class WrongNetworkConditionException extends DownloadException {
	public WrongNetworkConditionException(String message) {
		super(message);
	}
}
