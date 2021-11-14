package de.christinecoenen.code.zapp.tv.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.leanback.widget.BaseCardView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.TvChannelCardBinding
import kotlin.math.roundToInt

class ChannelCardView : BaseCardView {

	private lateinit var binding: TvChannelCardBinding

	constructor(
		context: Context?,
		attrs: AttributeSet?,
		defStyleAttr: Int
	) : super(context, attrs, defStyleAttr) {
		buildCardView(attrs, defStyleAttr, R.style.Widget_Leanback_ImageCardView)
	}

	constructor(
		context: Context?,
		attrs: AttributeSet?
	) : this(context, attrs, R.attr.imageCardViewStyle)

	constructor(context: Context?) : this(context, null)


	private fun buildCardView(attrs: AttributeSet?, defStyleAttr: Int, defStyle: Int) {
		isFocusable = true
		isFocusableInTouchMode = true

		val inflater = LayoutInflater.from(context)
		binding = TvChannelCardBinding.inflate(inflater, this, true)
	}

	fun setLogo(@DrawableRes logoResId: Int) {
		binding.logo.setImageResource(logoResId)
	}

	fun setChannelSubtitle(channelSubtitle: String?) {
		binding.channelSubtitle.isVisible = !channelSubtitle.isNullOrEmpty()
		binding.channelSubtitle.text = channelSubtitle
	}

	fun setShowTitle(showTitle: String?) {
		binding.showTitle.text = showTitle
	}

	fun setShowSubtitle(showSubtitle: String?) {
		binding.showSubtitle.isVisible = !showSubtitle.isNullOrEmpty()
		binding.showSubtitle.text = showSubtitle
	}

	fun setShowTime(showTime: String?) {
		binding.showTime.isVisible = !showTime.isNullOrEmpty()
		binding.showTime.text = showTime
	}

	fun setshowProgress(showProgress: Float?) {
		if (showProgress == null) {
			binding.showProgress.isVisible = false
		} else {
			binding.showProgress.isIndeterminate = false
			binding.showProgress.isVisible = true
			binding.showProgress.progress = (showProgress * binding.showProgress.max).roundToInt()
		}
	}

	fun setLoading() {
		setShowTitle(null)
		setShowSubtitle(null)
		setShowTime(null)

		binding.showProgress.isIndeterminate = true
		binding.showProgress.isVisible = true
	}
}
