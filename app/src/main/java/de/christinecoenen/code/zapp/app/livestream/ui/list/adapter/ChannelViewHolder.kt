package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.view.View
import android.view.View.OnLongClickListener
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ProgramInfoViewModel
import de.christinecoenen.code.zapp.databinding.FragmentChannelListItemBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import kotlinx.coroutines.Job
import kotlin.math.roundToInt

class ChannelViewHolder(
	private val binding: FragmentChannelListItemBinding,
	private val lifecycleOwner: LifecycleOwner,
	private val listener: ListItemListener
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, OnLongClickListener {

	private var channel: ChannelModel? = null
	private var loadingJob: Job? = null
	private var currentViewModel: ProgramInfoViewModel? = null

	init {
		binding.root.setOnLongClickListener(this)
		binding.root.setOnClickListener(this)
	}

	override fun onClick(view: View) {
		channel?.let { listener.onItemClick(it) }
	}

	override fun onLongClick(view: View): Boolean {
		channel?.let { listener.onItemLongClick(it, view) }
		return true
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
		binding.textShowTitle.text = title
	}

	private fun onShowSubtitleChanged(subtitle: String?) {
		binding.textShowSubtitle.isVisible = !subtitle.isNullOrEmpty()
		binding.textShowSubtitle.text = subtitle
	}

	private fun onShowTimeChanged(time: String?) {
		binding.textShowTime.isVisible = !time.isNullOrEmpty()
		binding.textShowTime.text = time
	}

	private fun onShowProgressPercentChanged(progressPercent: Float?) {
		binding.progressbarShowProgress.isIndeterminate = false

		if (progressPercent == null) {
			binding.progressbarShowProgress.isEnabled = false
		} else {
			val progress = (progressPercent * binding.progressbarShowProgress.max).roundToInt()
			binding.progressbarShowProgress.progress = progress
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
		binding.logo.setImageResource(channel.drawableId)
		binding.logo.contentDescription = channel.name

		binding.subtitle.text = channel.subtitle
		binding.subtitle.isVisible = !channel.subtitle.isNullOrEmpty()
	}

	private fun setViewLoading() {
		binding.textShowTitle.text = ""
		binding.textShowSubtitle.text = ""
		binding.textShowTime.text = ""
		binding.progressbarShowProgress.progress = 0
		binding.progressbarShowProgress.isEnabled = true
		binding.progressbarShowProgress.isIndeterminate = true
	}
}
