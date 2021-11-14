package de.christinecoenen.code.zapp.tv.player

import android.os.Bundle
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import org.koin.android.ext.android.inject

class PlayerFragment : VideoSupportFragment() {

	private lateinit var transportControlGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>

	private val player: Player by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val channel =
			activity?.intent?.getSerializableExtra(PlayerActivity.EXTRA_CHANNEL) as ChannelModel?
				?: throw IllegalArgumentException("channel extra has to be set")

		val glueHost = VideoSupportFragmentGlueHost(this@PlayerFragment)

		val playerAdapter = LeanbackPlayerAdapter(requireContext(), player.exoPlayer, 1000)
		playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

		transportControlGlue = PlaybackTransportControlGlue(activity, playerAdapter)
		transportControlGlue.host = glueHost
		transportControlGlue.title = channel.name
		transportControlGlue.isSeekEnabled = true
		transportControlGlue.playWhenPrepared()

		// TODO: add error handling

		lifecycleScope.launchWhenCreated {
			player.load(VideoInfo.fromChannel(channel))
			player.resume()
		}
	}

	override fun onPause() {
		super.onPause()
		transportControlGlue.pause()
	}

	override fun onDestroy() {
		super.onDestroy()

		player.pause()
		player.exoPlayer.release()
	}
}
