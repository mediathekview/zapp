package de.christinecoenen.code.zapp.app.downloads.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.christinecoenen.code.zapp.repositories.MediathekRepository


class DownloadsViewModelFactory(val mediathekRepository: MediathekRepository) :
	ViewModelProvider.NewInstanceFactory() {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel?> create(modelClass: Class<T>): T = DownloadsViewModel(mediathekRepository) as T
}
