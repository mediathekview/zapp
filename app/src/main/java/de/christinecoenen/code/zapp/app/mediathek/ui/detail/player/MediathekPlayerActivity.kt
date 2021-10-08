package de.christinecoenen.code.zapp.app.mediathek.ui.detail.player

import android.content.Context
import android.content.Intent
import de.christinecoenen.code.zapp.app.player.AbstractPlayerActivity
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.app.player.VideoInfo.Companion.fromShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.IntentHelper.openUrl
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject

class MediathekPlayerActivity : AbstractPlayerActivity() {

	companion object {
		private const val EXTRA_PERSISTED_SHOW_ID =
			"de.christinecoenen.code.zapp.EXTRA_PERSISTED_SHOW_ID"

		@JvmStatic
		fun getStartIntent(context: Context?, persistedShowId: Int): Intent {
			return Intent(context, MediathekPlayerActivity::class.java)
				.apply {
					action = Intent.ACTION_VIEW
					putExtra(EXTRA_PERSISTED_SHOW_ID, persistedShowId)
				}
		}
	}

	private val mediathekRepository: MediathekRepository by inject()

	private var persistedShow: PersistedMediathekShow? = null


	private fun onShowLoaded(persistedMediathekShow: PersistedMediathekShow) {
		persistedShow = persistedMediathekShow

		if (supportActionBar != null) {
			title = persistedMediathekShow.mediathekShow.topic
			supportActionBar!!.subtitle = persistedMediathekShow.mediathekShow.title
		}
	}

	override fun onShareMenuItemClicked() {
		openUrl(this, persistedShow!!.mediathekShow.getVideoUrl(Quality.Medium))
	}

	override suspend fun getVideoInfoFromIntent(intent: Intent): VideoInfo {
		val persistedShowId = intent.extras?.getInt(EXTRA_PERSISTED_SHOW_ID, 0)

		if (persistedShowId == null || persistedShowId == 0) {
			throw IllegalArgumentException("PersistedShowId id is not allowed to be null.")
		}

		val persistedMediathekShow = mediathekRepository.getPersistedShow(persistedShowId).first()
		onShowLoaded(persistedMediathekShow)

		return fromShow(persistedMediathekShow)
	}
}
