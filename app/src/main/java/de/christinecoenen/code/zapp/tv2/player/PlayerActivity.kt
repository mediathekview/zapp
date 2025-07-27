package de.christinecoenen.code.zapp.tv2.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import org.koin.android.ext.android.inject

class PlayerActivity : ComponentActivity() {

	companion object {

		private const val EXTRA_VIDEO_INFO: String = "EXTRA_VIDEO_INFO"

		fun getStartIntent(context: Context, videoInfo: VideoInfo): Intent {
			return Intent(context, PlayerActivity::class.java).apply {
				putExtra(EXTRA_VIDEO_INFO, videoInfo)
			}
		}

	}

	private val player: Player by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val videoInfo = intent?.getSerializableExtra(EXTRA_VIDEO_INFO) as VideoInfo?
			?: throw IllegalArgumentException("videoInfo extra has to be set")

		setContent {
			AppTheme {
				PlayerScreen(
					videoInfo = videoInfo,
					player = player,
				)
			}
		}
	}

}
