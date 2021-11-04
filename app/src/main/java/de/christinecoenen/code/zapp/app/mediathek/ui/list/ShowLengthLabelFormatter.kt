package de.christinecoenen.code.zapp.app.mediathek.ui.list

import com.google.android.material.slider.LabelFormatter
import de.christinecoenen.code.zapp.utils.view.ShowDurationFormatter

class ShowLengthLabelFormatter(private val maxValue: Float) : LabelFormatter {

	override fun getFormattedValue(value: Float): String {
		if (value.compareTo(0) == 0) {
			return "0"
		}
		if (value.compareTo(maxValue) == 0) {
			return "∞"
		}

		return ShowDurationFormatter.formatMinutes(value.toInt())
	}

}
