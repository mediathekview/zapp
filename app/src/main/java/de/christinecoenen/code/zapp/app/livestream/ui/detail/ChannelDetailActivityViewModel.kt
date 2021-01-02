package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.repositories.ChannelRepository


class ChannelDetailActivityViewModel(
	channelRepository: ChannelRepository,
	private val settingsRepository: SettingsRepository
) : ViewModel() {

	val channelList = channelRepository.getChannelList()

	val screenOrientation: Int
		get() = if (settingsRepository.lockVideosInLandcapeFormat) {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
		} else {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR
		}

	fun getChannelPosition(channelId: String) = channelList.indexOf(channelId)

}
