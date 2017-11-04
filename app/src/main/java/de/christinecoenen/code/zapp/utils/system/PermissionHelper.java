package de.christinecoenen.code.zapp.utils.system;


import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

public class PermissionHelper {

	private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

	public static boolean writeExternalStorageAllowed(Fragment fragment) {
		if (ActivityCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
			return false;
		} else {
			return true;
		}
	}

}
