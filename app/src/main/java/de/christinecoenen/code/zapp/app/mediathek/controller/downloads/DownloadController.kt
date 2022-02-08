package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.content.Context
import android.net.ConnectivityManager
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2.Fetch.Impl.getInstance
import com.tonyodev.fetch2.database.DownloadInfo
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader.FileDownloaderType
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.NoNetworkException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DownloadController(
	applicationContext: Context,
	private val scope: CoroutineScope,
	private val mediathekRepository: MediathekRepository
) : FetchListener, IDownloadController {

	private lateinit var fetch: Fetch
	private val connectivityManager: ConnectivityManager =
		applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
	private val settingsRepository: SettingsRepository = SettingsRepository(applicationContext)
	private val downloadFileInfoManager: DownloadFileInfoManager =
		DownloadFileInfoManager(applicationContext, settingsRepository)

	init {
		val cookieManager = CookieManager()
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

		val client: OkHttpClient = OkHttpClient.Builder()
			.readTimeout(40, TimeUnit.SECONDS) // fetch default: 20 seconds
			.connectTimeout(30, TimeUnit.SECONDS) // fetch default: 15 seconds
			.cache(null)
			.followRedirects(true)
			.followSslRedirects(true)
			.retryOnConnectionFailure(false)
			.cookieJar(JavaNetCookieJar(cookieManager))
			.build()

		val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(applicationContext)
			.setNotificationManager(object :
				ZappNotificationManager(applicationContext, scope, mediathekRepository) {
				override fun getFetchInstanceForNamespace(namespace: String): Fetch {
					return fetch
				}
			})
			.enableRetryOnNetworkGain(false)
			.setAutoRetryMaxAttempts(0)
			.setDownloadConcurrentLimit(1)
			.preAllocateFileOnCreation(false) // true causes downloads to sd card to hang
			.setHttpDownloader(OkHttpDownloader(client, FileDownloaderType.SEQUENTIAL))
			.enableLogging(true)
			.build()

		fetch = getInstance(fetchConfiguration)
		fetch.addListener(this)
	}

	override suspend fun startDownload(show: PersistedMediathekShow, quality: Quality) {
		val downloadUrl = show.mediathekShow.getVideoUrl(quality)
			?: throw DownloadException("$quality is no valid download quality.")

		val download = getDownload(show.downloadId)

		if (download.id != 0 && download.url == downloadUrl) {
			// same quality as existing download
			// save new settings to request
			applySettingsToRequest(download.request)
			fetch.updateRequest(download.id, download.request, false, null, null)

			// update show properties
			show.downloadedAt = DateTime.now()
			show.downloadProgress = 0

			mediathekRepository.updateShow(show)

			// retry
			fetch.retry(show.downloadId)
		} else {
			// delete old file with wrong quality
			fetch.delete(show.downloadId)

			val filePath =
				downloadFileInfoManager.getDownloadFilePath(show.mediathekShow, quality)

			val request: Request
			try {
				request = Request(downloadUrl, filePath)
				request.identifier = show.id.toLong()
			} catch (e: Exception) {
				throw DownloadException("Constructing download request failed.", e)
			}

			// update show properties
			show.downloadId = request.id
			show.downloadedAt = DateTime.now()
			show.downloadProgress = 0

			mediathekRepository.updateShow(show)

			enqueueDownload(request)
		}
	}

	override fun stopDownload(persistedShowId: Int) {
		fetch.getDownloadsByRequestIdentifier(persistedShowId.toLong()) { downloadList ->
			for (download in downloadList) {
				fetch.cancel(download.id)
			}
		}
	}

	override fun deleteDownload(persistedShowId: Int) {
		fetch.getDownloadsByRequestIdentifier(persistedShowId.toLong()) { downloadList ->
			for (download in downloadList) {
				fetch.delete(download.id)
			}
		}
	}

	override fun getDownloadStatus(persistedShowId: Int): Flow<DownloadStatus> {
		return mediathekRepository.getDownloadStatus(persistedShowId)
	}

	override fun getDownloadProgress(persistedShowId: Int): Flow<Int> {
		return mediathekRepository.getDownloadProgress(persistedShowId)
	}

	override fun deleteDownloadsWithDeletedFiles() {
		fetch.getDownloadsWithStatus(Status.COMPLETED) { downloads ->
			for (download in downloads) {
				if (downloadFileInfoManager.shouldDeleteDownload(download)) {
					fetch.remove(download.id)
				}
			}
		}
	}

	/**
	 * @return download with the given id or empty download with id of 0
	 */
	private suspend fun getDownload(downloadId: Int): Download = suspendCoroutine { continuation ->
		fetch.getDownload(downloadId) { download ->
			if (download == null) {
				continuation.resume(DownloadInfo())
			} else {
				continuation.resume(download)
			}
		}
	}

	private fun enqueueDownload(request: Request) {
		applySettingsToRequest(request)
		fetch.enqueue(request, null, null)
	}

	private fun applySettingsToRequest(request: Request) {
		request.networkType = if (settingsRepository.downloadOverUnmeteredNetworkOnly) {
			NetworkType.UNMETERED
		} else {
			NetworkType.ALL
		}

		if (connectivityManager.activeNetwork == null) {
			throw NoNetworkException("No active network available.")
		}
		if (settingsRepository.downloadOverUnmeteredNetworkOnly && connectivityManager.isActiveNetworkMetered) {
			throw WrongNetworkConditionException("Download over metered networks prohibited.")
		}
	}

	private suspend fun updateDownloadStatus(download: Download) {
		val downloadStatus = DownloadStatus.values()[download.status.value]
		mediathekRepository.updateDownloadStatus(download.id, downloadStatus)
	}

	private suspend fun updateDownloadProgress(download: Download, progress: Int) {
		mediathekRepository.updateDownloadProgress(download.id, progress)
	}

	override fun onAdded(download: Download) {
		scope.launch {
			updateDownloadStatus(download)
		}
	}

	override fun onCancelled(download: Download) {
		scope.launch {
			fetch.delete(download.id)
			updateDownloadStatus(download)
			updateDownloadProgress(download, 0)
		}
	}

	override fun onCompleted(download: Download) {
		scope.launch {
			updateDownloadStatus(download)
			mediathekRepository.updateDownloadedVideoPath(download.id, download.file)
			downloadFileInfoManager.updateDownloadFileInMediaCollection(download)
		}
	}

	override fun onDeleted(download: Download) {
		scope.launch {
			updateDownloadStatus(download)
			updateDownloadProgress(download, 0)
			downloadFileInfoManager.updateDownloadFileInMediaCollection(download)
		}
	}

	override fun onDownloadBlockUpdated(
		download: Download,
		downloadBlock: DownloadBlock,
		totalBlocks: Int
	) {
	}

	override fun onError(download: Download, error: Error, throwable: Throwable?) {
		scope.launch {
			downloadFileInfoManager.deleteDownloadFile(download)
			updateDownloadStatus(download)
		}
	}

	override fun onPaused(download: Download) {
		scope.launch {
			updateDownloadStatus(download)
		}
	}

	override fun onProgress(
		download: Download,
		etaInMilliSeconds: Long,
		downloadedBytesPerSecond: Long
	) {
		scope.launch {
			updateDownloadProgress(download, download.progress)
		}
	}

	override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
		scope.launch {
			updateDownloadStatus(download)
		}
	}

	override fun onRemoved(download: Download) {
		scope.launch {
			updateDownloadStatus(download)
		}
	}

	override fun onResumed(download: Download) {
		scope.launch {
			updateDownloadStatus(download)
		}
	}

	override fun onStarted(
		download: Download,
		downloadBlocks: List<DownloadBlock>,
		totalBlocks: Int
	) {
		scope.launch {
			updateDownloadStatus(download)
		}
	}

	override fun onWaitingNetwork(download: Download) {
		scope.launch {
			updateDownloadStatus(download)
		}
	}
}
