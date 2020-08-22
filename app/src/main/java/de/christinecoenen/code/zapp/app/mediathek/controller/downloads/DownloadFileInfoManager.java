package de.christinecoenen.code.zapp.app.mediathek.controller.downloads;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.tonyodev.fetch2.Download;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository;

class DownloadFileInfoManager {

	private final Context applicationContext;
	private final SettingsRepository settingsRepository;

	DownloadFileInfoManager(Context applicationContext, SettingsRepository settingsRepository) {
		this.applicationContext = applicationContext;
		this.settingsRepository = settingsRepository;
	}

	boolean shouldDeleteDownload(Download download) {
		String filePath = download.getFile();

		if (isMediaStoreFile(filePath)) {
			return isDeletedMediaStoreFile(filePath);
		}

		File downloadFile = new File(download.getFile());
		return !downloadFile.exists() &&
			Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(downloadFile));
	}

	String getDownloadFilePath(MediathekShow show, Quality quality) {
		String fileName = show.getDownloadFileName(quality);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			return getMediaStoreUri(show).toString();
		} else {
			return getExternalMediaDirPath(fileName);
		}
	}

	void updateDownloadFileInMediaCollection(@NonNull Download download) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			String filePath = download.getFileUri().getPath();
			MediaScannerConnection.scanFile(applicationContext, new String[]{filePath}, new String[]{"video/*"}, null);
		} else {
			ContentResolver resolver = applicationContext.getContentResolver();
			switch (download.getStatus()) {
				case DELETED:
					try {
						resolver.delete(download.getFileUri(), null, null);
					} catch (SecurityException e) {
						// maybe file is already deleted - that's okay
					}
					break;
				case COMPLETED:
					ContentValues videoContentValues = new ContentValues();
					videoContentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
					resolver.update(download.getFileUri(), videoContentValues, null, null);
					break;
			}
		}
	}

	private String getExternalMediaDirPath(String fileName) {
		File downloadFile = null;

		File[] externalMediaDirs = applicationContext.getExternalMediaDirs();
		File sdCardDir = externalMediaDirs.length > 1 ? externalMediaDirs[1] : null;

		if (settingsRepository.getDownloadToSdCard() && sdCardDir != null &&
			Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(sdCardDir))) {
			downloadFile = new File(sdCardDir, fileName);
		}

		if (downloadFile == null) {
			downloadFile = new File(externalMediaDirs[0], fileName);
		}

		return downloadFile.getAbsolutePath();
	}

	@RequiresApi(api = Build.VERSION_CODES.Q)
	private Uri getMediaStoreUri(MediathekShow mediathekShow) {
		Set<String> volumeNames = MediaStore.getExternalVolumeNames(applicationContext);

		String sdcardVoumeName = volumeNames
			.stream()
			.filter(name -> !MediaStore.VOLUME_EXTERNAL_PRIMARY.equals(name))
			.findFirst()
			.orElse(null);

		String primaryVolumeName = volumeNames
			.stream()
			.filter(MediaStore.VOLUME_EXTERNAL_PRIMARY::equals)
			.findFirst()
			.orElse(null);
		assert primaryVolumeName != null;

		String volumeName = settingsRepository.getDownloadToSdCard() && sdcardVoumeName != null ?
			sdcardVoumeName : primaryVolumeName;

		Uri downloadFileUri = getMediaStoreUriForVolume(volumeName, mediathekShow);

		if (downloadFileUri == null &&
			settingsRepository.getDownloadToSdCard() &&
			volumeName.equals(sdcardVoumeName)) {
			// most likely the external sd card is not properly writable - fall back to primary storage
			downloadFileUri = getMediaStoreUriForVolume(primaryVolumeName, mediathekShow);
		}

		return downloadFileUri;
	}

	@RequiresApi(api = Build.VERSION_CODES.Q)
	private Uri getMediaStoreUriForVolume(@NonNull String volumeName, MediathekShow mediathekShow) {
		Uri videoMediaStoreUri = MediaStore.Video.Media.getContentUri(volumeName);

		ContentValues videoContentValues = new ContentValues();
		videoContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, mediathekShow.getTitle());
		videoContentValues.put(MediaStore.Video.Media.DESCRIPTION, mediathekShow.getDescription());
		videoContentValues.put(MediaStore.Video.Media.CATEGORY, mediathekShow.getChannel());
		videoContentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Zapp");
		videoContentValues.put(MediaStore.Video.Media.IS_PENDING, 1);

		ContentResolver resolver = applicationContext.getContentResolver();
		Uri fileUri = resolver.insert(videoMediaStoreUri, videoContentValues);

		if (fileUri == null) {
			// Insertion failed because somewhere in MediaStore this file already exists.
			// We cannot query for it, because we have no id and display name may have been changed
			// by the system or app may no longer have rights to access it.
			// We create a new file with slightly altered title as workaround.
			videoContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, mediathekShow.getTitle() + " (" + UUID.randomUUID().toString().substring(0, 5) + ")");

			fileUri = resolver.insert(videoMediaStoreUri, videoContentValues);
		}

		return fileUri;
	}

	private boolean isMediaStoreFile(String path) {
		return path.startsWith("content:/");
	}

	private boolean isDeletedMediaStoreFile(String path) {
		Uri uri = Uri.parse(path);
		String[] projection = new String[]{MediaStore.Video.Media._ID};

		Cursor cursor = applicationContext
			.getContentResolver()
			.query(uri, projection, null, null, null, null);

		if (cursor != null && cursor.getCount() == 1) {
			// item is still there - do not delete!
			cursor.close();
			return false;
		}

		return true;
	}
}
