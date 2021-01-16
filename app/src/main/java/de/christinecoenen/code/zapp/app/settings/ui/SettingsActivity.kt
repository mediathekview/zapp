package de.christinecoenen.code.zapp.app.settings.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import de.christinecoenen.code.zapp.app.settings.ui.SettingsFragment.Companion.newInstance

class SettingsActivity : AppCompatActivity() {

	companion object {

		@JvmStatic
		fun getStartIntent(context: Context): Intent {
			return Intent(context, SettingsActivity::class.java)
		}

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		// display the fragment as the main content.
		supportFragmentManager.beginTransaction()
			.replace(android.R.id.content, newInstance())
			.commit()
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			android.R.id.home -> {
				finish()
				true
			}
			else -> {
				super.onOptionsItemSelected(item)
			}
		}
	}
}
