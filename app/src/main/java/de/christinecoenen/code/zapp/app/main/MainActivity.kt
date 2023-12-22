package de.christinecoenen.code.zapp.app.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.ActivityMainBinding
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), MenuProvider {

	private var _binding: ActivityMainBinding? = null
	private val binding get() = _binding!!

	private val settingsRepository: SettingsRepository by inject()

	private lateinit var navController: NavController
	private lateinit var appBarConfiguration: AppBarConfiguration

	private val requestPermissionLauncher =
		registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		_binding = ActivityMainBinding.inflate(layoutInflater)

		setContentView(binding.root)

		val startFragmentId = settingsRepository.startFragment

		navController = binding.navHostFragment.getFragment<NavHostFragment>().navController
		navController.graph.setStartDestination(startFragmentId)

		appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.channelListFragment,
				R.id.mediathekListFragment,
				R.id.personalFragment,
			),
			fallbackOnNavigateUpListener = ::onSupportNavigateUp
		)

		setSupportActionBar(binding.toolbar)
		setupActionBarWithNavController(navController, appBarConfiguration)

		navController.addOnDestinationChangedListener(::onDestinationChanged)

		binding.bottomNavigation.setupWithNavController(navController)

		addMenuProvider(this)

		PreferenceManager.setDefaultValues(application, R.xml.preferences, false)

		if (savedInstanceState == null &&
			navController.currentDestination?.id == R.id.channelListFragment
		) {
			// new app start - explicitly navigate to start fragment defined in settings
			navController.navigate(startFragmentId)
		}

		requestPermissions()

		if (!settingsRepository.dynamicColors) {
			// original zapp colors always require light status bar text (independent from theme)
			SystemUiHelper.useLightStatusBar(window, false)
		}
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
		} else {
			binding.toolbar.logo = null
			binding.toolbar.titleMarginStart = 0
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		// done by child fragments
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return menuItem.onNavDestinationSelected(navController)
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}

	private fun requestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(
					this,
					Manifest.permission.POST_NOTIFICATIONS
				) == PackageManager.PERMISSION_GRANTED
			) {
				return
			}

			requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
		}
	}
}
