package de.christinecoenen.code.zapp.app.livestream.ui.views

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.app.livestream.repository.ChannelInfoRepository
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

abstract class ProgramInfoViewBase @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

	private val uiHandler = Handler(Looper.getMainLooper())

	private val updateShowInfoIntervalSeconds =
		resources.getInteger(R.integer.view_program_info_update_show_info_interval_seconds)

	private val updateShowTimeIntervalSeconds =
		resources.getInteger(R.integer.view_program_info_update_show_time_interval_seconds)

	private var currentShow: LiveShow? = null
	private var currentChannel: ChannelModel? = null
	private var timer: Timer? = null
	private var showJob: Job? = null

	private val showTitleView: TextView by lazy {
		rootView.findViewById(R.id.text_show_title)
	}
	private val showSubtitleView: TextView by lazy {
		rootView.findViewById(R.id.text_show_subtitle)
	}
	private val showTimeView: TextView by lazy {
		rootView.findViewById(R.id.text_show_time)
	}
	private val progressBarView: ProgressBar by lazy {
		rootView.findViewById(R.id.progressbar_show_progress)
	}

	private val showProgressAnimator: ObjectAnimator by lazy {
		val animator = AnimatorInflater.loadAnimator(context, R.animator.view_program_info_show_progress) as ObjectAnimator
		animator.target = progressBarView
		animator
	}


	init {
		orientation = VERTICAL
		gravity = Gravity.CENTER_VERTICAL
	}


	fun setChannel(channel: ChannelModel) {
		if (channel === currentChannel) {
			return
		}

		currentShow = null
		currentChannel = channel

		showTitleView.text = ""
		showSubtitleView.text = ""
		showTimeView.text = ""
		progressBarView.progress = 0

		chancelProgramGuideLoading()
		loadProgramGuide()
	}

	fun pause() {
		timer?.cancel()
		timer = null
	}

	fun resume() {
		if (timer != null) {
			return
		}

		timer = Timer().apply {
			scheduleAtFixedRate(UpdateShowInfoTask(), 0,
				TimeUnit.SECONDS.toMillis(updateShowInfoIntervalSeconds.toLong()))
			scheduleAtFixedRate(UpdateShowTimeTask(), 0,
				TimeUnit.SECONDS.toMillis(updateShowTimeIntervalSeconds.toLong()))
		}
	}

	private fun onRequestError(e: Throwable) {
		logMessage("could not load show info: " + e.message)

		currentShow = null

		showTitleView.setText(R.string.activity_channel_detail_info_error)

		showSubtitleView.isVisible = false
		showTimeView.isVisible = false
		progressBarView.isVisible = false
	}

	private fun onRequestSuccess(newShow: LiveShow) {
		logMessage("show info loaded: ${newShow.title}")

		currentShow = newShow

		displayTitles()
		displayTime()
	}

	private fun updateShowInfo() {
		if (currentShow?.endTime?.isBeforeNow == true) {
			reloadProgramGuide()
		}
	}

	private fun displayTime() {
		if (currentShow == null) {
			return
		}

		if (currentShow!!.hasDuration()) {
			val progressPercent = currentShow!!.progressPercent
			val progress = (progressPercent * progressBarView.max).roundToInt()

			val startTime = DateUtils.formatDateTime(context,
				currentShow!!.startTime!!.millis, DateUtils.FORMAT_SHOW_TIME)
			val endTime = DateUtils.formatDateTime(context,
				currentShow!!.endTime!!.millis, DateUtils.FORMAT_SHOW_TIME)
			val fullTime = context.getString(R.string.view_program_info_show_time, startTime, endTime)

			showTimeView.text = fullTime
			showTimeView.isVisible = true
			progressBarView.isIndeterminate = false
			progressBarView.isEnabled = true
			setShowProgressBar(progress)
		} else {
			setShowProgressBar(0)
			showTimeView.isVisible = false
			progressBarView.isIndeterminate = false
			progressBarView.isEnabled = false
		}

		progressBarView.isVisible = true
	}

	private fun displayTitles() {
		if (currentShow == null) {
			return
		}

		showTitleView.text = currentShow!!.title

		if (currentShow!!.hasSubtitle()) {
			showSubtitleView.text = currentShow!!.subtitle
			showSubtitleView.isVisible = true
		} else {
			showSubtitleView.isVisible = false
		}
	}

	private fun reloadProgramGuide() {
		if (currentChannel == null) {
			return
		}

		logMessage("reloadProgramGuide")
		chancelProgramGuideLoading()

		loadProgramGuide()
	}

	private fun loadProgramGuide() {
		progressBarView.isEnabled = true
		progressBarView.isIndeterminate = true

		showJob = GlobalScope.launch(Dispatchers.Main) {
			try {
				val show = ChannelInfoRepository.getInstance().getShows(currentChannel!!.id)
				onRequestSuccess(show)
			} catch (e: Exception) {
				onRequestError(e)
			}
		}
	}

	private fun chancelProgramGuideLoading() {
		progressBarView.isIndeterminate = false
		showJob?.cancel()
		showJob = null
	}

	private fun setShowProgressBar(value: Int) {
		showProgressAnimator.setIntValues(value)
		showProgressAnimator.start()
	}

	private fun logMessage(message: String) {
		Timber.d("${currentChannel?.id} - $message")
	}

	private inner class UpdateShowTimeTask : TimerTask() {
		override fun run() {
			uiHandler.post { displayTime() }
		}
	}

	private inner class UpdateShowInfoTask : TimerTask() {
		override fun run() {
			uiHandler.post {
				logMessage("UpdateShowInfoTask")
				updateShowInfo()
			}
		}
	}
}
