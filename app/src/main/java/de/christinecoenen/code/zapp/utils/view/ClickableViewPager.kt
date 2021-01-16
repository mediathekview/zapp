package de.christinecoenen.code.zapp.utils.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class ClickableViewPager @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

	private var listener: OnClickListener? = null

	private var gestureDetector: GestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
		override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
			listener?.onClick(null)
			return true
		}
	})

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		gestureDetector.onTouchEvent(event)
		return super.onTouchEvent(event)
	}

	override fun setOnClickListener(listener: OnClickListener?) {
		super.setOnClickListener(listener)
		this.listener = listener
	}
}
