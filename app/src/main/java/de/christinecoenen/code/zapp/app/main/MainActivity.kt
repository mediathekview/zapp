package de.christinecoenen.code.zapp.app.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.search.SearchView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.search.SearchViewModel
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.ActivityMainBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), MenuProvider {

	private var _binding: ActivityMainBinding? = null
	private val binding get() = _binding!!

	private val settingsRepository: SettingsRepository by inject()

	private val searchViewModel: SearchViewModel by viewModel()

	private lateinit var navController: NavController
	private lateinit var appBarConfiguration: AppBarConfiguration

	private val requestPermissionLauncher =
		registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

	private val onSearchViewPressedCallback = object : OnBackPressedCallback(true) {
		override fun handleOnBackPressed() {
			searchViewModel.exit()
		}
	}

	private val searchViewTransistionListener = SearchView.TransitionListener { _, _, newState ->
		val isShown = newState == SearchView.TransitionState.SHOWN
		val isHidden = newState == SearchView.TransitionState.HIDDEN

		onSearchViewPressedCallback.isEnabled = isShown

		updateBottomNavigationVisibility()

		// set correct state when user pressed back button on searchView
		if (isHidden) {
			when (navController.currentDestination?.id) {
				R.id.searchResultsFragment -> searchViewModel.submit()
				else -> searchViewModel.exit()
			}
		}
	}

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

		onSearchViewPressedCallback.isEnabled = false
		onBackPressedDispatcher.addCallback(this, onSearchViewPressedCallback)

		binding.searchView.let {
			it.addTransitionListener(searchViewTransistionListener)
			it.editText.addTextChangedListener { editable ->
				searchViewModel.setSearchQuery(editable.toString())
			}
			it.editText.setOnFocusChangeListener { _, hasFocus ->
				if (hasFocus) {
					searchViewModel.enterQueryMode()
				}
			}
			it.editText.setOnEditorActionListener { _, _, _ ->
				onSubmitSearch()
				return@setOnEditorActionListener false
			}
		}

		launchOnCreated {
			searchViewModel.searchQuery.collectLatest {
				if (binding.searchView.text.toString() != it) {
					binding.searchView.setText(it)
				}
			}
		}

		launchOnCreated {
			searchViewModel.searchState.drop(1).collectLatest { searchState ->
				val query = binding.searchView.text.toString()

				when (searchState) {
					SearchViewModel.SeachState.None -> {
						binding.searchView.hide()
					}

					SearchViewModel.SeachState.Query -> {
						binding.searchView.show()
						binding.searchView.setText(query)
						binding.searchView.editText.setSelection(query.length)
					}

					SearchViewModel.SeachState.Results -> {
						binding.searchView.hide()
						navController.navigate(R.id.searchResultsFragment,
							bundleOf("title" to query),
							navOptions { launchSingleTop = true }
						)
					}
				}
			}
		}

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

		// show bottom navigation for main destinations
		updateBottomNavigationVisibility()

		// show search for non destinations
		binding.searchbar.isVisible = isMainDestination

		binding.toolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
			scrollFlags = if (isMainDestination) {
				SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
			} else {
				SCROLL_FLAG_NO_SCROLL
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		navController.removeOnDestinationChangedListener(::onDestinationChanged)
		_binding?.searchView?.removeTransitionListener(searchViewTransistionListener)
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

	private fun onSubmitSearch() {
		binding.fragmentSearch.requestFocus()
		searchViewModel.submit()
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

	private fun updateBottomNavigationVisibility() {
		val arguments = navController.currentDestination?.arguments
		val isMainDestination = arguments?.get("is_main_destination")?.defaultValue == true

		val isSearchOverlayVisible =
			binding.searchView.currentTransitionState == SearchView.TransitionState.SHOWN ||
				binding.searchView.currentTransitionState == SearchView.TransitionState.SHOWING

		binding.bottomNavigation.isVisible = isMainDestination && !isSearchOverlayVisible
	}
}
