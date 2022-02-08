package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.view.View
import android.view.View.OnLongClickListener
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
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

	private fun onShowTitleChanged(title: String) {
		bindingAdapter.showTitle.text = title
	}

	private fun onShowSubtitleChanged(subtitle: String?) {
		bindingAdapter.showSubtitle.isVisible = !subtitle.isNullOrEmpty()
		bindingAdapter.showSubtitle.text = subtitle
	}

	private fun onShowTimeChanged(time: String?) {
		bindingAdapter.showTime.isVisible = !time.isNullOrEmpty()
		bindingAdapter.showTime.text = time
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
		currentViewModel?.title?.observe(lifecycleOwner, ::onShowTitleChanged)
		currentViewModel?.subtitle?.observe(lifecycleOwner, ::onShowSubtitleChanged)
		currentViewModel?.time?.observe(lifecycleOwner, ::onShowTimeChanged)
		currentViewModel?.progressPercent?.observe(lifecycleOwner, ::onShowProgressPercentChanged)

		// start program info loading
		loadingJob = lifecycleOwner.lifecycleScope.launchWhenCreated {
			currentViewModel?.setChannelId(channel.id)
		}
	}

	private fun stopLoadingProgramInfo() {
		// cancel program info loading
		loadingJob?.cancel()
		loadingJob = null

		// remove observers from last view model
		currentViewModel?.title?.removeObservers(lifecycleOwner)
		currentViewModel?.title?.removeObservers(lifecycleOwner)
		currentViewModel?.subtitle?.removeObservers(lifecycleOwner)
		currentViewModel?.time?.removeObservers(lifecycleOwner)
		currentViewModel?.progressPercent?.removeObservers(lifecycleOwner)
		currentViewModel = null
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
