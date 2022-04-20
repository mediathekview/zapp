package de.christinecoenen.code.zapp.app.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	private var _binding: ActivityMainBinding? = null
	private val binding get() = _binding!!

	private lateinit var navController: NavController
	private lateinit var appBarConfiguration: AppBarConfiguration

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		_binding = ActivityMainBinding.inflate(layoutInflater)

		setContentView(binding.root)

		navController = binding.navHostFragment.getFragment<NavHostFragment>().navController

		appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.channelListFragment,
				R.id.mediathekListFragment,
				R.id.downloadsFragment,
			)
		)
		binding.toolbar.setupWithNavController(navController, appBarConfiguration)
		setSupportActionBar(binding.toolbar)

		binding.bottomNavigation.setupWithNavController(navController)

		PreferenceManager.setDefaultValues(application, R.xml.preferences, false)
	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == android.R.id.home) {
			return onSupportNavigateUp()
		}
		return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}
}
