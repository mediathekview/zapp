package de.christinecoenen.code.zapp.app.player

import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository


class AbstractPlayerActivityViewModel(
	private val settingsRepository: SettingsRepository
) : ViewModel() {

	val screenOrientation: Int
		get() = if (settingsRepository.lockVideosInLandcapeFormat) {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
		} else {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR
		}

}
