package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

class DownloadFileInfoManager(
	private val applicationContext: Context,
	private val settingsRepository: SettingsRepository
) {

	fun deleteDownloadFile(filePath: String) {
		val file = File(filePath)

		try {
			file.delete()
		} catch (e: Exception) {
			Timber.w(e)
		}

		updateDownloadFileInMediaCollection(
			Uri.parse(filePath),
			DownloadStatus.DELETED
		)
	}

	fun shouldDeleteDownload(show: PersistedMediathekShow): Boolean {
		val filePath = show.downloadedVideoPath ?: return false

		if (isMediaStoreFile(filePath)) {
			return isDeletedMediaStoreFile(filePath)
		}

		val downloadFile = File(filePath)
		return !downloadFile.exists() &&
			Environment.MEDIA_MOUNTED == Environment.getExternalStorageState(downloadFile)
	}

	fun getDownloadFilePath(show: MediathekShow, quality: Quality): String {
		val fileName = show.getDownloadFileName(quality)

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			getMediaStoreUri(show).toString()
		} else {
			getExternalMediaDirPath(fileName)
		}
	}

	fun updateDownloadFileInMediaCollection(filePathUri: Uri, status: DownloadStatus) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			val filePath = filePathUri.path
			MediaScannerConnection.scanFile(
				applicationContext,
				arrayOf(filePath),
				arrayOf("video/*"),
				null
			)
		} else {
			val resolver = applicationContext.contentResolver

			when (status) {
				DownloadStatus.DELETED,
				DownloadStatus.FAILED -> try {
					resolver.delete(filePathUri, null, null)
				} catch (e: SecurityException) {
					// maybe file is already deleted - that's okay
				} catch (e: IllegalArgumentException) {
					// most likely the external volume has been removed -
					// its not worth raising this error
					Timber.e(e)
				}

				DownloadStatus.COMPLETED -> {
					val videoContentValues = ContentValues()
					videoContentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
					try {
						resolver.update(filePathUri, videoContentValues, null, null)
					} catch (e: SecurityException) {
						// maybe file is already deleted - that's okay - db entry will adapt
					}
				}

				else -> {}
			}
		}
	}

	fun getFileSize(filePathUri: String): Long {
		return try {
			if (isMediaStoreFile(filePathUri)) {
				val uri = Uri.parse(filePathUri)
				val pfd = applicationContext.contentResolver.openFileDescriptor(uri, "r")
				val fileLength = pfd?.statSize ?: 0
				pfd?.close()
				fileLength
			} else {
				File(filePathUri).length()
			}
		} catch (e: FileNotFoundException) {
			0
		}
	}

	fun openOutputStream(filePathUri: String, append: Boolean = false): OutputStream? {

		return if (isMediaStoreFile(filePathUri)) {
			val uri = Uri.parse(filePathUri)
			val mode = if (append) "wa" else "w"
			applicationContext.contentResolver.openOutputStream(uri, mode)
		} else {
			val file = File(filePathUri)

			// create if not exists
			if (!file.exists()) {
				if (!file.createNewFile()) {
					return null
				}
			}

			return FileOutputStream(file, append)
		}
	}

	private fun getExternalMediaDirPath(fileName: String): String {
		var downloadFile: File? = null
		val externalMediaDirs = applicationContext.externalMediaDirs
		val sdCardDir = if (externalMediaDirs.size > 1) externalMediaDirs[1] else null

		if (settingsRepository.downloadToSdCard &&
			sdCardDir != null &&
			Environment.MEDIA_MOUNTED == Environment.getExternalStorageState(sdCardDir)
		) {
			downloadFile = File(sdCardDir, fileName)
		}

		downloadFile = downloadFile ?: File(externalMediaDirs[0], fileName)

		return downloadFile.absolutePath
	}

	@RequiresApi(api = Build.VERSION_CODES.Q)
	private fun getMediaStoreUri(mediathekShow: MediathekShow): Uri? {
		val volumeNames = MediaStore.getExternalVolumeNames(applicationContext)

		val sdcardVoumeName = volumeNames
			.stream()
			.filter { it != MediaStore.VOLUME_EXTERNAL_PRIMARY }
			.findFirst()
			.orElse(null)

		val primaryVolumeName = volumeNames
			.stream()
			.filter { it == MediaStore.VOLUME_EXTERNAL_PRIMARY }
			.findFirst()
			.get()

		val volumeName = if (settingsRepository.downloadToSdCard && sdcardVoumeName != null) {
			sdcardVoumeName
		} else {
			primaryVolumeName
		}

		var downloadFileUri = getMediaStoreUriForVolume(volumeName, mediathekShow)

		if (downloadFileUri == null &&
			settingsRepository.downloadToSdCard &&
			volumeName == sdcardVoumeName
		) {
			// most likely the external sd card is not properly writable - fall back to primary storage
			downloadFileUri = getMediaStoreUriForVolume(primaryVolumeName, mediathekShow)
		}

		return downloadFileUri
	}

	@RequiresApi(api = Build.VERSION_CODES.Q)
	private fun getMediaStoreUriForVolume(volumeName: String, mediathekShow: MediathekShow): Uri? {
		val videoMediaStoreUri = MediaStore.Video.Media.getContentUri(volumeName)

		val videoContentValues = ContentValues().apply {
			put(MediaStore.Video.Media.DISPLAY_NAME, mediathekShow.title)
			put(MediaStore.Video.Media.DESCRIPTION, mediathekShow.description)
			put(MediaStore.Video.Media.CATEGORY, mediathekShow.channel)
			put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Zapp")
			put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
			put(MediaStore.Video.Media.IS_PENDING, 1)
		}

		val resolver = applicationContext.contentResolver
		var fileUri = resolver.insert(videoMediaStoreUri, videoContentValues)

		if (fileUri == null) {
			// Insertion failed because somewhere in MediaStore this file already exists.
			// We cannot query for it, because we have no id and display name may have been changed
			// by the system or app may no longer have rights to access it.
			// We create a new file with slightly altered title as workaround.
			videoContentValues.put(
				MediaStore.Video.Media.DISPLAY_NAME,
				mediathekShow.title + " (" + UUID.randomUUID().toString().substring(0, 5) + ")"
			)
			fileUri = resolver.insert(videoMediaStoreUri, videoContentValues)
		}

		return fileUri
	}

	private fun isMediaStoreFile(path: String): Boolean {
		return path.startsWith("content:/")
	}

	private fun isDeletedMediaStoreFile(path: String): Boolean {
		val uri = Uri.parse(path)
		val projection = arrayOf(MediaStore.Video.Media._ID)

		try {
			val cursor = applicationContext.contentResolver
				.query(uri, projection, null, null, null, null)

			if (cursor != null && cursor.count == 1) {
				// item is still there - do not delete!
				cursor.close()
				return false
			}
		} catch (e: IllegalArgumentException) {
			// IllegalArgumentException due to storage no longer existing - mark as deleted.
			// In future we might want to indicate an error instead.
			return true
		}

		return true
	}
}
