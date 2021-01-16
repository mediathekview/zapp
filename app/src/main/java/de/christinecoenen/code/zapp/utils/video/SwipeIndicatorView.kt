package de.christinecoenen.code.zapp.utils.video

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ViewSwipeIndicatorBinding

class SwipeIndicatorView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

	private var binding: ViewSwipeIndicatorBinding

	init {
		val view = inflate(context, R.layout.view_swipe_indicator, this)
		binding = ViewSwipeIndicatorBinding.bind(view)

		visibility = GONE
	}

	fun setIconResId(resId: Int) {
		binding.icon.setImageResource(resId)
	}

	fun setValue(value: Float) {
		binding.indicator.layoutParams.height = (value * height).toInt()
		binding.indicator.requestLayout()

		visibility = VISIBLE
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()

		val params = layoutParams as LayoutParams
		params.height = LayoutParams.MATCH_PARENT
		params.width = 300

		layoutParams = params
	}
}
