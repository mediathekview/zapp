package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.app.player.AbstractPlayerActivity
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.utils.system.ShortcutHelper
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

	override val shouldShowOverlay = true

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.channel.observe(this, ::onChannelLoaded)
		viewModel.previousChannelLiveData.observe(this, ::onPrevChannelLoaded)
		viewModel.nextChannelLiveData.observe(this, ::onNextChannelLoaded)
		programInfoViewModel.title.observe(this, ::onShowTitleChanged)
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		super.onCreateMenu(menu, menuInflater)

		menuInflater.inflate(R.menu.activity_channel_player, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		when (menuItem.itemId) {
			R.id.menu_program_info -> {
				val modalBottomSheet = ProgramInfoSheetDialogFragment(
					programInfoViewModel,
					ProgramInfoSheetDialogFragment.Size.Small
				)
				modalBottomSheet.show(supportFragmentManager, ProgramInfoSheetDialogFragment.TAG)
				return true
			}
		}

		return super.onMenuItemSelected(menuItem)
	}

	override fun onShareMenuItemClicked() {
		val channel = viewModel.channel.value ?: return
		channel.playExternally(this)
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
		title = channel.name
	}

	private fun onPrevChannelLoaded(channel: ChannelModel?) {
		binding.btnPrev.isEnabled = channel != null

		if (channel == null) {
			return
		}

		binding.btnPrev.setOnClickListener {
			startActivity(getStartIntent(this, channel.id))
		}
	}

	private fun onNextChannelLoaded(channel: ChannelModel?) {
		binding.btnNext.isEnabled = channel != null

		if (channel == null) {
			return
		}

		binding.btnNext.setOnClickListener {
			startActivity(getStartIntent(this, channel.id))
		}
	}

	private fun onShowTitleChanged(sshowTitle: String) {
		supportActionBar?.let {
			it.subtitle = sshowTitle
		}
	}
}
