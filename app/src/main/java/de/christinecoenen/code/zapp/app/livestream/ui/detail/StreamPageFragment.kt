package de.christinecoenen.code.zapp.app.livestream.ui.detail


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.model.ChannelModel
import kotlinx.android.synthetic.main.fragment_stream_page.*

class StreamPageFragment : Fragment() {

	private val logoView: ImageView by lazy { image_channel_logo }
	private val subtitleText: TextView by lazy { text_channel_subtitle }
	private val errorText: TextView by lazy { text_error }

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_stream_page, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val channel = arguments!!.getSerializable(ARGUMENT_CHANNEL_MODEL) as ChannelModel

		logoView.setImageResource(channel.drawableId)
		logoView.contentDescription = channel.name

		errorText.setBackgroundColor(channel.color)

		subtitleText.text = channel.subtitle ?: ""
	}

	override fun onStop() {
		super.onStop()
		// don't use onPause to support multiwindow feature
		view!!.visibility = View.VISIBLE
		errorText.visibility = View.GONE
	}

	fun onHide() {
		view!!.visibility = View.VISIBLE
		errorText.visibility = View.GONE
	}

	fun onVideoStart() {
		fadeOutLogo()
	}

	fun onVideoError(message: String) {
		view!!.visibility = View.VISIBLE
		errorText.visibility = View.VISIBLE
		errorText.text = message
	}

	private fun fadeOutLogo() {
		if (view!!.visibility == View.VISIBLE) {
			val fadeOutAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
			fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
				override fun onAnimationStart(animation: Animation) {}

				override fun onAnimationEnd(animation: Animation) {
					view!!.visibility = View.GONE
				}

				override fun onAnimationRepeat(animation: Animation) {}
			})

			view!!.startAnimation(fadeOutAnimation)
		}
	}

	companion object {

		private const val ARGUMENT_CHANNEL_MODEL = "ARGUMENT_CHANNEL_MODEL"

		@JvmStatic
		fun newInstance(channelModel: ChannelModel): StreamPageFragment {
			val fragment = StreamPageFragment()
			val args = Bundle()
			args.putSerializable(ARGUMENT_CHANNEL_MODEL, channelModel)
			fragment.arguments = args
			return fragment
		}
	}
}
