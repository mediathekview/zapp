package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.NoNetworkException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.flow.Flow

interface IDownloadController {

	/**
	 * Triggers the download of the given show in the given [Quality].
	 * This method is responsible to update download information of the given [PersistedMediathekShow]
	 * inside the [MediathekRepository], including [PersistedMediathekShow.downloadId] and
	 * [PersistedMediathekShow.downloadProgress].
	 *
	 * @throws DownloadException When the given quality is not available or on other errors.
	 * @throws WrongNetworkConditionException When user has prohibited downloads for the network type currently used.
	 * @throws NoNetworkException When no network connection is there to download.
	 */
	suspend fun startDownload(persistedShowId: Int, quality: Quality)

	/**
	 * Cancels the download with the given id and deletes all related (partly) downloaded files.
	 */
	fun stopDownload(persistedShowId: Int)

	/**
	 * Deletes a fully downloaded show with all related files.
	 */
	fun deleteDownload(persistedShowId: Int)

	/**
	 * Queries all fully downloaded shows and removes them from the database when their corresponding
	 * files have been deleted by the user.
	 */
	fun deleteDownloadsWithDeletedFiles()

	fun getDownloadStatus(persistedShowId: Int): Flow<DownloadStatus>

	/**
	 * Download progress so far in percent (between 0 and including 100).
	 */
	fun getDownloadProgress(persistedShowId: Int): Flow<Int>
}
