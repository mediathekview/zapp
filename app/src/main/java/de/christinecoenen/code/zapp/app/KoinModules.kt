package de.christinecoenen.code.zapp.app

import de.christinecoenen.code.zapp.app.livestream.api.IZappBackendApiService
import de.christinecoenen.code.zapp.app.livestream.api.ZappBackendApiServiceFactory
import de.christinecoenen.code.zapp.app.livestream.repository.ProgramInfoRepository
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelPlayerActivityViewModel
import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekApiServiceFactory
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadFileInfoManager
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.WorkManagerDownloadController
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelperViewModel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentViewModel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.filter.MediathekFilterViewModel
import de.christinecoenen.code.zapp.app.personal.PersonalViewModel
import de.christinecoenen.code.zapp.app.personal.details.BookmarksViewModel
import de.christinecoenen.code.zapp.app.personal.details.ContinueWatchingViewModel
import de.christinecoenen.code.zapp.app.personal.details.DownloadsViewModel
import de.christinecoenen.code.zapp.app.player.AbstractPlayerActivityViewModel
import de.christinecoenen.code.zapp.app.player.IPlaybackPositionRepository
import de.christinecoenen.code.zapp.app.player.PersistedPlaybackPositionRepository
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.search.SearchViewModel
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import de.christinecoenen.code.zapp.persistence.Database
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.repositories.QuerySubscriptionRepository
import de.christinecoenen.code.zapp.repositories.SearchRepository
import de.christinecoenen.code.zapp.utils.api.UserAgentInterceptor
import io.noties.markwon.Markwon
import kotlinx.coroutines.MainScope
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

class KoinModules {

	companion object {

		val AppModule = module {
			single {
				OkHttpClient.Builder()
					.addInterceptor(UserAgentInterceptor())
					.build()
			}

			single { MainScope() }

			single { Markwon.create(androidContext()) }

			single { ChannelRepository(androidContext(), get(), get()) }
			single { Database.getInstance(androidContext()) }
			single { MediathekRepository(get()) }
			single { SearchRepository(get()) }
			single { QuerySubscriptionRepository(get()) }
			single { PersistedPlaybackPositionRepository(get()) } bind IPlaybackPositionRepository::class
			single {
				WorkManagerDownloadController(
					androidContext(),
					get(),
					get(),
					get(),
					get()
				)
			} bind IDownloadController::class
			single {
				ZappBackendApiServiceFactory(androidContext(), get()).create()
			} bind IZappBackendApiService::class
			single {
				MediathekApiServiceFactory(androidContext(), get()).create()
			} bind IMediathekApiService::class
			single { ProgramInfoRepository(get()) }
			single { DownloadFileInfoManager(get(), get()) }

			factory { SettingsRepository(androidContext()) }
			factory { Player(androidContext(), get(), get(), get()) }
			factory { JsonChannelList(androidContext()) }

			viewModel { AbstractPlayerActivityViewModel(get()) }
			viewModel { ChannelPlayerActivityViewModel(get()) }
			viewModel { PersonalViewModel(get()) }
			viewModel { BookmarksViewModel(get()) }
			viewModel { ContinueWatchingViewModel(get()) }
			viewModel { DownloadsViewModel(get()) }
			viewModel { ProgramInfoViewModel(androidApplication(), get()) }
			viewModel { MediathekListFragmentViewModel(get(), get()) }
			viewModel { MediathekFilterViewModel() }
			viewModel { ShowMenuHelperViewModel(get(), get()) }
			viewModel { SearchViewModel(get(), get(), get(), get()) }
		}

	}

}
