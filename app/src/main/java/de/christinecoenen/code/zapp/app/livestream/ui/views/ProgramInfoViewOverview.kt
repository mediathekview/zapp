package de.christinecoenen.code.zapp.app.livestream.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import de.christinecoenen.code.zapp.R

class ProgramInfoViewOverview @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : ProgramInfoViewBase(context, attrs) {

	init {
		val inflater = context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

		inflater.inflate(R.layout.view_program_info_overview, this, true)
	}

}
