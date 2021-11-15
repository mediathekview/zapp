package de.christinecoenen.code.zapp.tv.error

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import de.christinecoenen.code.zapp.R

class CrashActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.tv_activity_crash)
	}

}
