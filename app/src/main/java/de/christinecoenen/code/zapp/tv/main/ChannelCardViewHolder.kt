package de.christinecoenen.code.zapp.tv.main

import android.graphics.drawable.InsetDrawable
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import kotlinx.coroutines.Job
import kotlin.math.max
import kotlin.math.roundToInt

class ChannelCardViewHolder(
	private val imageCardView: ImageCardView,
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
		imageCardView.contentText = title
	}

	private fun onShowSubtitleChanged(subtitle: String?) {

	}

	private fun onShowTimeChanged(time: String?) {

	}

	private fun onShowProgressPercentChanged(progressPercent: Float?) {

	}

	private fun setViewToChannel(channel: ChannelModel) {
		imageCardView.setMainImageDimensions(200, 100)
		imageCardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER)
		imageCardView.setMainImageAdjustViewBounds(true)

		val logoDrawable = AppCompatResources.getDrawable(view.context, channel.drawableId)!!
		val inset =
			(max(logoDrawable.intrinsicHeight, logoDrawable.intrinsicWidth) * 0.25).roundToInt()
		imageCardView.titleText = channel.name
		imageCardView.mainImage = InsetDrawable(logoDrawable, inset)
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
		imageCardView.contentText = "..."
	}
}
