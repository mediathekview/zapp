package de.christinecoenen.code.zapp.app

import de.christinecoenen.code.zapp.app.downloads.ui.list.DownloadsViewModel
import de.christinecoenen.code.zapp.app.livestream.repository.ChannelInfoRepository
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelPlayerActivityViewModel
import de.christinecoenen.code.zapp.app.main.MainViewModel
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekApi
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController
import de.christinecoenen.code.zapp.app.player.AbstractPlayerActivityViewModel
import de.christinecoenen.code.zapp.app.player.IPlaybackPositionRepository
import de.christinecoenen.code.zapp.app.player.PersistedPlaybackPositionRepository
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import de.christinecoenen.code.zapp.persistence.Database
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

class KoinModules {

	companion object {

		val AppModule = module {
			single { ChannelRepository(androidContext(), get()) }
			single { Database.getInstance(androidContext()) }
			single { MediathekRepository(get(), get()) }
			single { PersistedPlaybackPositionRepository(get()) } bind IPlaybackPositionRepository::class
			single { DownloadController(androidContext(), get()) }
			single { MediathekApi() }
			single { ChannelInfoRepository() }

			factory { SettingsRepository(androidContext()) }
			factory { Player(androidContext(), get()) }
			factory { JsonChannelList(androidContext()) }

			viewModel { MainViewModel(androidApplication()) }
			viewModel { AbstractPlayerActivityViewModel(get()) }
			viewModel { ChannelPlayerActivityViewModel(get()) }
			viewModel { DownloadsViewModel(get()) }
			viewModel { ProgramInfoViewModel(androidApplication(), get()) }
		}

	}

}
