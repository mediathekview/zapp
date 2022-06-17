package de.christinecoenen.code.zapp.tv.player

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import de.christinecoenen.code.zapp.app.player.Player
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.tv.error.ErrorActivity
import org.koin.android.ext.android.inject

class PlayerFragment : VideoSupportFragment() {

	private lateinit var transportControlGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>

	private val player: Player by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val videoInfo =
			activity?.intent?.getSerializableExtra(PlayerActivity.EXTRA_VIDEO_INFO) as VideoInfo?
				?: throw IllegalArgumentException("videoInfo extra has to be set")

		val glueHost = VideoSupportFragmentGlueHost(this@PlayerFragment)

		val playerAdapter = LeanbackPlayerAdapter(requireContext(), player.exoPlayer, 1000)
		playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

		transportControlGlue = PlaybackTransportControlGlue(activity, playerAdapter)
		transportControlGlue.host = glueHost
		transportControlGlue.title = videoInfo.title
		transportControlGlue.subtitle = videoInfo.subtitle
		transportControlGlue.isSeekEnabled = true
		transportControlGlue.playWhenPrepared()

		lifecycleScope.launchWhenCreated {
			player.load(videoInfo)
			player.resume()
		}

		lifecycleScope.launchWhenCreated {
			player.errorResourceId.collect {
				if (it == null || it == -1) {
					return@collect
				}

				onError(it)
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		player.setView(view)
	}

	override fun onPause() {
		super.onPause()
		transportControlGlue.pause()
	}

	override fun onDestroy() {
		lifecycleScope.launchWhenCreated {
			player.destroy()
		}

		super.onDestroy()
	}

	private fun onError(@StringRes messageResId: Int) {
		val message = getString(messageResId)
		val intent = ErrorActivity.getStartIntent(requireContext(), message)
		startActivity(intent)
		requireActivity().finish()
	}
}
