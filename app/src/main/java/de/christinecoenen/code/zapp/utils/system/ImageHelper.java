package de.christinecoenen.code.zapp.utils.system;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import java.io.File;
import java.io.IOException;

public class ImageHelper {

	public static Bitmap loadThumbnail(String filePath) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			return ThumbnailUtils.createVideoThumbnail(new File(filePath), new Size(640, 360), null);
		} else {
			//noinspection deprecation
			return ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
		}
	}
}
