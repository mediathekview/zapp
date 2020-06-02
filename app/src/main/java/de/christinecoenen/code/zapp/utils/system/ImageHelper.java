package de.christinecoenen.code.zapp.utils.system;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import java.io.File;
import java.io.IOException;

public class ImageHelper {

	private static final Size THUMBNAIL_SIZE = new Size(640, 240);

	public static Bitmap loadThumbnail(Context context, String filePath) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ContentResolver contentResolver = context.getContentResolver();

			try {
				return contentResolver.loadThumbnail(Uri.parse(filePath), THUMBNAIL_SIZE, null);
			} catch (IOException e) {
				return ThumbnailUtils.createVideoThumbnail(new File(filePath), THUMBNAIL_SIZE, null);
			}

		} else {
			//noinspection deprecation
			return ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
		}
	}
}
