package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.databinding.FragmentStreamPageBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel

class StreamPageFragment : Fragment() {

	companion object {
		private const val ARGUMENT_CHANNEL_MODEL = "ARGUMENT_CHANNEL_MODEL"

		fun newInstance(channelModel: ChannelModel?): StreamPageFragment {
			return StreamPageFragment().apply {
				arguments = Bundle().apply {
					putSerializable(ARGUMENT_CHANNEL_MODEL, channelModel)
				}
			}
		}
	}

	private var _binding: FragmentStreamPageBinding? = null
	private val binding: FragmentStreamPageBinding get() = _binding!!

	private var listener: Listener? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentStreamPageBinding.inflate(inflater, container, false)

		val channel = requireArguments().getSerializable(ARGUMENT_CHANNEL_MODEL) as ChannelModel?
			?: throw IllegalArgumentException("channel argument is null")

		binding.logo.setImageResource(channel.drawableId)
		binding.logo.contentDescription = channel.name

		binding.textError.setBackgroundColor(channel.color)
		binding.textError.setOnClickListener { onErrorViewClick() }

		if (channel.subtitle != null) {
			binding.subtitle.text = channel.subtitle
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		listener = if (context is Listener) {
			context
		} else {
			throw RuntimeException("Activity must implement StreamPageFragment.Listener.")
		}
	}

	override fun onDetach() {
		super.onDetach()
		listener = null
	}

	override fun onStop() {
		super.onStop()

		// don't use onPause to support multiwindow feature
		binding.root.isVisible = true
		binding.textError.isVisible = false
	}

	fun onHide() {
		binding.root.isVisible = true
		binding.textError.isVisible = false
	}

	fun onVideoStart() {
		fadeOutLogo()
	}

	fun onVideoError(message: String?) {
		binding.root.isVisible = true
		binding.textError.isVisible = true
		binding.textError.text = message
	}

	private fun onErrorViewClick() {
		listener?.onErrorViewClicked()
		onHide()
	}

	private fun fadeOutLogo() {
		if (!binding.root.isVisible) {
			return
		}

		val fadeOutAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)

		fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
			override fun onAnimationRepeat(animation: Animation) {}
			override fun onAnimationStart(animation: Animation) {}
			override fun onAnimationEnd(animation: Animation) {
				binding.root.isVisible = false
			}
		})

		binding.root.startAnimation(fadeOutAnimation)
	}

	interface Listener {
		fun onErrorViewClicked()
	}
}
