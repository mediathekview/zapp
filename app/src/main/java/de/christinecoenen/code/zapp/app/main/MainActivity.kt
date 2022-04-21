package de.christinecoenen.code.zapp.app.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
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
			),
			fallbackOnNavigateUpListener = ::onSupportNavigateUp
		)

		setSupportActionBar(binding.toolbar)
		setupActionBarWithNavController(navController, appBarConfiguration)

		navController.addOnDestinationChangedListener(::onDestinationChanged)

		binding.bottomNavigation.setupWithNavController(navController)

		PreferenceManager.setDefaultValues(application, R.xml.preferences, false)
	}

	@Suppress("UNUSED_PARAMETER")
	private fun onDestinationChanged(
		controller: NavController,
		destination: NavDestination,
		arguments: Bundle?
	) {
		val isMainDestination = arguments?.getBoolean("is_main_destination", false) == true

		// hide bottom navigation for non main destinations
		binding.bottomNavigation.isVisible = isMainDestination

		// hide toolbar logo for non main destinations
		if (isMainDestination) {
			binding.toolbar.setLogo(R.drawable.ic_zapp_tv_small)
			binding.toolbar.titleMarginStart =
				resources.getDimensionPixelSize(R.dimen.toolbar_logo_margin)
		} else {
			binding.toolbar.logo = null
			binding.toolbar.titleMarginStart = 0
		}
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
		return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}
}
