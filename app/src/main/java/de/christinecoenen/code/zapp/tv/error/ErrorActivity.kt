package de.christinecoenen.code.zapp.tv.error

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import de.christinecoenen.code.zapp.R

class ErrorActivity : FragmentActivity() {

	companion object {

		const val EXTRA_MESSAGE: String = "EXTRA_MESSAGE"

		fun getStartIntent(context: Context, message: String): Intent {
			return Intent(context, ErrorActivity::class.java).apply {
				putExtra(EXTRA_MESSAGE, message)
			}
		}

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.tv_activity_error)
	}

}
