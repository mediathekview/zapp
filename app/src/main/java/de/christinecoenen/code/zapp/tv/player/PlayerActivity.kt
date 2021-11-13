package de.christinecoenen.code.zapp.tv.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.channels.ChannelModel

class PlayerActivity : FragmentActivity() {

	companion object {

		const val EXTRA_CHANNEL: String = "EXTRA_CHANNEL"

		fun getStartIntent(context: Context, channel: ChannelModel): Intent {
			return Intent(context, PlayerActivity::class.java).apply {
				putExtra(EXTRA_CHANNEL, channel)
			}
		}

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.tv_activity_player)
	}
}
