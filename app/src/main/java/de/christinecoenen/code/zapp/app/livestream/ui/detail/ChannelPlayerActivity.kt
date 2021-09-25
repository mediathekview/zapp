package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.player.AbstractPlayerActivity
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper
import de.christinecoenen.code.zapp.utils.view.ColorHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelPlayerActivity : AbstractPlayerActivity() {

	companion object {

		private const val EXTRA_CHANNEL_ID = "de.christinecoenen.code.zapp.EXTRA_CHANNEL_ID"

		@JvmStatic
		fun getStartIntent(context: Context?, channelId: String?): Intent {
			return Intent(context, ChannelPlayerActivity::class.java).apply {
				action = Intent.ACTION_VIEW
				putExtra(EXTRA_CHANNEL_ID, channelId)
			}
		}

	}

	private val viewModel: ChannelPlayerActivityViewModel by viewModel()
	private val programInfoViewModel: ProgramInfoViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.channel.observe(this, ::onChannelLoaded)
		programInfoViewModel.title.observe(this, ::onShowTitleChanged)
	}

	override fun onShareMenuItemClicked() {
		val channel = viewModel.channel.value ?: return

		startActivity(
			Intent.createChooser(channel.videoShareIntent, getString(R.string.action_share))
		)
	}

	override suspend fun getVideoInfoFromIntent(intent: Intent): VideoInfo {
		val channelId = intent.extras?.getString(EXTRA_CHANNEL_ID)
			?: throw IllegalArgumentException("Channel id is not allowed to be null.")

		ShortcutHelper.reportShortcutUsageGuarded(this, channelId)

		val channel = viewModel.setChannelId(channelId)
		programInfoViewModel.setChannelId(channelId)

		return VideoInfo.fromChannel(channel)
	}

	private fun onChannelLoaded(channel: ChannelModel) {
		// TODO: display channel logo
		// TODO: display program information
		title = channel.name
		setColor(channel.color)
	}

	private fun onShowTitleChanged(sshowTitle: String) {
		supportActionBar?.let {
			it.subtitle = sshowTitle
		}
	}

	private fun setColor(color: Int) {
		binding.toolbar.setBackgroundColor(color)
		binding.error.setBackgroundColor(color)

		window.statusBarColor = ColorHelper.darker(color, 0.075f)
	}
}
