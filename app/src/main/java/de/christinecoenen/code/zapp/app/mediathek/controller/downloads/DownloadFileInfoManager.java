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

		String volumeName = null;

		if (settingsRepository.getDownloadToSdCard()) {
			volumeName = volumeNames
				.stream()
				.filter(name -> !MediaStore.VOLUME_EXTERNAL_PRIMARY.equals(name))
				.findFirst()
				.orElse(null);
		}

		if (volumeName == null) {
			volumeName = volumeNames
				.stream()
				.filter(MediaStore.VOLUME_EXTERNAL_PRIMARY::equals)
				.findFirst()
				.orElse(null);
		}

		Uri videoMediaStoreUri = MediaStore.Video.Media.getContentUri(volumeName);

		ContentValues videoContentValues = new ContentValues();
		videoContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, mediathekShow.getTitle());
		videoContentValues.put(MediaStore.Video.Media.DESCRIPTION, mediathekShow.getDescription());
		videoContentValues.put(MediaStore.Video.Media.CATEGORY, mediathekShow.getChannel());

		ContentResolver resolver = applicationContext.getContentResolver();
		return resolver.insert(videoMediaStoreUri, videoContentValues);
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
