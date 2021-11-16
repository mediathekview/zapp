package de.christinecoenen.code.zapp.tv.main.channels

import androidx.leanback.widget.Presenter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import kotlinx.coroutines.Job

class ChannelCardViewHolder(
	private val imageCardView: ChannelCardView,
	private val lifecycleOwner: LifecycleOwner
) : Presenter.ViewHolder(imageCardView) {

	private var channel: ChannelModel? = null
	private var loadingJob: Job? = null
	private var currentViewModel: ProgramInfoViewModel? = null

	init {
		recycle()
	}

	fun setChannel(programInfoViewModel: ProgramInfoViewModel, channel: ChannelModel) {
		this.currentViewModel = programInfoViewModel
		this.channel = channel

		setViewToChannel(channel)
		startLoadingProgramInfo(channel)
	}

	fun recycle() {
		stopLoadingProgramInfo()
		setViewLoading()
	}

	private fun onShowTitleChanged(title: String) {
		imageCardView.setShowTitle(title)
	}

	private fun onShowSubtitleChanged(subtitle: String?) {
		imageCardView.setShowSubtitle(subtitle)
	}

	private fun onShowTimeChanged(time: String?) {
		imageCardView.setShowTime(time)
	}

	private fun onShowProgressPercentChanged(progressPercent: Float?) {
		imageCardView.setshowProgress(progressPercent)
	}

	private fun setViewToChannel(channel: ChannelModel) {
		imageCardView.setChannelSubtitle(channel.subtitle)
		imageCardView.setLogo(channel.drawableId)
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

	private fun setViewLoading() {
		imageCardView.setLoading()
	}
}
