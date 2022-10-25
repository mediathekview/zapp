package de.christinecoenen.code.zapp.tv.main

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import de.christinecoenen.code.zapp.R

class MainActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.tv_activity_main)

		onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				val mainFragment =
					supportFragmentManager.findFragmentById(R.id.main_fragment) as MainFragment

				// give MainFragment the change to hadle back presses by itself
				val backPressedHandled = mainFragment.onBackPressed()

				if (!backPressedHandled) {
					finish()
				}
			}
		})
	}
}
