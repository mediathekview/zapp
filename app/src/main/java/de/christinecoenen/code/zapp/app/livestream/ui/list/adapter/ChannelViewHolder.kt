package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.view.View
import android.view.View.OnLongClickListener
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import kotlinx.coroutines.Job
import kotlin.math.roundToInt

class ChannelViewHolder(
	private val bindingAdapter: IChannelViewBindingAdapter,
	private val lifecycleOwner: LifecycleOwner,
	private val listener: ListItemListener
) : RecyclerView.ViewHolder(bindingAdapter.rootView), View.OnClickListener, OnLongClickListener {

	private var channel: ChannelModel? = null
	private var loadingJob: Job? = null
	private var currentViewModel: ProgramInfoViewModel? = null

	init {
		recycle()

		bindingAdapter.rootView.setOnLongClickListener(this)
		bindingAdapter.rootView.setOnClickListener(this)
	}

	override fun onClick(view: View) {
		channel?.let { listener.onItemClick(it) }
	}

	override fun onLongClick(view: View): Boolean {
		channel?.let { listener.onItemLongClick(it, view) }
		return true
	}

	fun setChannel(programInfoViewModel: ProgramInfoViewModel, channel: ChannelModel) {
		if (channel.id == this.channel?.id) {
			return
		}

		recycle()

		this.currentViewModel = programInfoViewModel
		this.channel = channel

		setViewToChannel(channel)
		startLoadingProgramInfo(channel)
	}

	fun recycle() {
		stopLoadingProgramInfo()
		setViewLoading()

		currentViewModel = null
		channel = null
	}

	private fun onShowChanged(liveShow: LiveShow) {
		// title
		bindingAdapter.showTitle.text =
			HtmlCompat.fromHtml(liveShow.title, HtmlCompat.FROM_HTML_MODE_LEGACY)

		// subtitle
		val subtitle = liveShow.subtitle
		bindingAdapter.showSubtitle.isVisible = !subtitle.isNullOrEmpty()

		if (!subtitle.isNullOrEmpty()) {
			bindingAdapter.showSubtitle.text =
				HtmlCompat.fromHtml(subtitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
		}

		// time
		val formattedTime = liveShow.formattedDuration(bindingAdapter.showTime.context)
		bindingAdapter.showTime.isVisible = formattedTime != null
		bindingAdapter.showTime.text = formattedTime
	}

	private fun onShowProgressPercentChanged(progressPercent: Float?) {
		bindingAdapter.showProgress.isIndeterminate = false

		if (progressPercent == null) {
			bindingAdapter.showProgress.isEnabled = false
		} else {
			val progress = (progressPercent * bindingAdapter.showProgress.max).roundToInt()
			bindingAdapter.showProgress.progress = progress
		}
	}

	private fun startLoadingProgramInfo(channel: ChannelModel) {
		// observe changes in view model
		currentViewModel?.liveShow?.observe(lifecycleOwner, ::onShowChanged)
		currentViewModel?.progressPercent?.observe(lifecycleOwner, ::onShowProgressPercentChanged)

		// start program info loading
		loadingJob = lifecycleOwner.launchOnCreated {
			currentViewModel?.setChannelId(channel.id)
		}
	}

	private fun stopLoadingProgramInfo() {
		// cancel program info loading
		loadingJob?.cancel()
		loadingJob = null

		// remove observers from last view model
		currentViewModel?.liveShow?.removeObservers(lifecycleOwner)
		currentViewModel?.progressPercent?.removeObservers(lifecycleOwner)
	}

	private fun setViewToChannel(channel: ChannelModel) {
		bindingAdapter.logo.setImageResource(channel.drawableId)
		bindingAdapter.logo.contentDescription = channel.name

		bindingAdapter.subtitle.text = channel.subtitle
		bindingAdapter.subtitle.isVisible = !channel.subtitle.isNullOrEmpty()
	}

	private fun setViewLoading() {
		bindingAdapter.showTitle.text = ""
		bindingAdapter.showSubtitle.text = ""
		bindingAdapter.showTime.text = ""
		bindingAdapter.showProgress.progress = 0
		bindingAdapter.showProgress.isEnabled = true
		bindingAdapter.showProgress.isIndeterminate = true
	}
}
