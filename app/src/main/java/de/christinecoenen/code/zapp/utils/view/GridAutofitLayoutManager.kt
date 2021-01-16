package de.christinecoenen.code.zapp.utils.view

import android.content.Context
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.max

/**
 * A GridLayoutManager with a flexible column count
 * based on view size and item size.
 *
 * @see "http://stackoverflow.com/a/30256880/3012757"
 */
class GridAutofitLayoutManager(context: Context, columnWidth: Int) : GridLayoutManager(context, 1) {

	private var mColumnWidth = 0
	private var mColumnWidthChanged = true

	private fun checkedColumnWidth(context: Context, columnWidth: Int): Int {
		var calculatedColumnWidth = columnWidth

		if (calculatedColumnWidth <= 0) {
			/* Set default columnWidth value (48dp here). It is better to move this constant
            to static constant on top, but we need context to convert it to dp, so can't really
            do so. */
			calculatedColumnWidth = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				48f,
				context.resources.displayMetrics
			).toInt()
		}

		return calculatedColumnWidth
	}

	private fun setColumnWidth(newColumnWidth: Int) {
		if (newColumnWidth > 0 && newColumnWidth != mColumnWidth) {
			mColumnWidth = newColumnWidth
			mColumnWidthChanged = true
		}
	}

	override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
		val width = width
		val height = height

		if (mColumnWidthChanged && mColumnWidth > 0 && width > 0 && height > 0) {
			val totalSpace = if (orientation == VERTICAL) {
				(width - paddingRight - paddingLeft).toFloat()
			} else {
				(height - paddingTop - paddingBottom).toFloat()
			}

			val spanCount = max(1f, totalSpace / mColumnWidth).toInt()
			setSpanCount(spanCount)

			mColumnWidthChanged = false
		}

		super.onLayoutChildren(recycler, state)
	}

	init {
		/* Initially set spanCount to 1, will be changed automatically later. */
		var calculatedColumnWidth = columnWidth

		// interprete as dip
		calculatedColumnWidth = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			calculatedColumnWidth.toFloat(),
			context.resources.displayMetrics
		).toInt()

		setColumnWidth(checkedColumnWidth(context, calculatedColumnWidth))
	}
}
