package de.christinecoenen.code.zapp.tv.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.player.VideoInfo

class PlayerActivity : FragmentActivity() {

	companion object {

		const val EXTRA_VIDEO_INFO: String = "EXTRA_VIDEO_INFO"

		fun getStartIntent(context: Context, videoInfo: VideoInfo): Intent {
			return Intent(context, PlayerActivity::class.java).apply {
				putExtra(EXTRA_VIDEO_INFO, videoInfo)
			}
		}

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.tv_activity_player)
	}
}
