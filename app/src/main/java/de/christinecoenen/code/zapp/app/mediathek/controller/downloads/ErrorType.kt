package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

/**
 * Types of errors that might occur during download.
 */
enum class ErrorType {
	/**
	 * We don't know what went wrong.
	 */
	Unknown,

	/**
	 * Something failed while initiating the worker.
	 * This should never happen.
	 */
	InitializationFailed,

	/**
	 * The server returned a 5xx response code.
	 */
	ServerError,

	/**
	 * The server returned a 404 or 410 response code.
	 * The file does not or no longer exist.
	 */
	FileNotFound,

	/**
	 * The server returned a 401, 403 or 451 response code.
	 * The file exists but is not available for the public -
	 * most likely due to geoblocking or age restriction.
	 */
	FileForbidden,

	/**
	 * The server returned a 429 response code.
	 */
	TooManyRequests,

	/**
	 * The server returned any 4xx reponse code not specified above.
	 */
	ClientError,

	/**
	 * Opening the output file on the device failed.
	 * Something may be wrong with the storage.
	 */
	FileWriteFailed,

	/**
	 * The socket closed during download - most likely
	 * due to network connection issues.
	 */
	FileReadFailed,
}
