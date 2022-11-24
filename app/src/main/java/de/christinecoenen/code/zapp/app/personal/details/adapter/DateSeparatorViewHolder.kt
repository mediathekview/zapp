package de.christinecoenen.code.zapp.app.personal.details.adapter

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentItemDateSeparatorBinding
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder

class DateSeparatorViewHolder(
	private val binding: MediathekListFragmentItemDateSeparatorBinding
) : RecyclerView.ViewHolder(binding.root) {

	companion object {
		var newFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
			.appendDayOfWeekText()
			.toFormatter()

		var thisYearFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
			.appendDayOfMonth(1)
			.appendLiteral(". ")
			.appendMonthOfYearShortText()
			.toFormatter()

		var oldFormatter: DateTimeFormatter = DateTimeFormat.mediumDate()
	}


	fun bind(date: LocalDate) {
		val now = LocalDate.now()

		if (date.isEqual(now)) {
			binding.text.setText(R.string.fragment_personal_today)
			return
		}

		val formatter = if (date.plusDays(4).isAfter(now)) {
			// is new
			newFormatter
		} else if (date.year == now.year) {
			// is this year
			thisYearFormatter
		} else {
			// before this year
			oldFormatter
		}

		binding.text.text = date.toString(formatter)
	}

}
