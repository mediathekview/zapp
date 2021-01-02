package de.christinecoenen.code.zapp.app.livestream.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.repositories.ChannelRepository


class ChannelDetailActivityViewModelFactory(
	private val channelRepository: ChannelRepository,
	private val settingsRepository: SettingsRepository
) :
	ViewModelProvider.NewInstanceFactory() {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel?> create(modelClass: Class<T>): T =
		ChannelDetailActivityViewModel(channelRepository, settingsRepository) as T
}
