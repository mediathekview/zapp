package de.christinecoenen.code.zapp.tv.error

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.ErrorSupportFragment
import de.christinecoenen.code.zapp.R

class ErrorFragment : ErrorSupportFragment() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		message = activity?.intent?.getStringExtra(ErrorActivity.EXTRA_MESSAGE)
			?: throw IllegalArgumentException("message extra has to be set")

		title = getString(R.string.error_informal)
		imageDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sad_tv)
		setDefaultBackground(false)
	}

}
