package de.christinecoenen.code.zapp.tv.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import de.christinecoenen.code.zapp.databinding.TvActivitySettingsBinding
import de.christinecoenen.code.zapp.utils.system.IStartableActivity

class SettingsActivity : FragmentActivity() {

	companion object : IStartableActivity {
		override fun getStartIntent(context: Context?): Intent =
			Intent(context, SettingsActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = TvActivitySettingsBinding.inflate(layoutInflater)

		setContentView(binding.root)
	}
}
