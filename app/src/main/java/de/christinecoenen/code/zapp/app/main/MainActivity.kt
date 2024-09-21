package de.christinecoenen.code.zapp.app.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUiSaveStateControl
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.search.SearchView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.search.SearchViewModel
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.ActivityMainBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyHorizontalInsetsAsPadding
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

	private val voiceInputLauncher =
		registerForActivityResult(
			ActivityResultContracts.StartActivityForResult(),
			::onVoiceInputReceived
		)

	private val searchViewTransistionListener = SearchView.TransitionListener { _, _, newState ->
		val isHidden = newState == SearchView.TransitionState.HIDDEN
		val isShown = newState == SearchView.TransitionState.SHOWN

		updateBottomNavigationVisibility()

		// set correct state when user shows the search view by clicking the search bar
		if (isShown) {
			searchViewModel.enterLastSearch()
		}

		// set correct state when user pressed back button on searchView
		if (isHidden) {
			when (navController.currentDestination?.id) {
				R.id.searchResultsFragment -> {}
				else -> searchViewModel.exitToNone()
			}
		}
	}

	@OptIn(NavigationUiSaveStateControl::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()

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

		NavigationUI.setupWithNavController(binding.bottomNavigation, navController, false)

		binding.searchView.let {
			it.addTransitionListener(searchViewTransistionListener)
			it.editText.addTextChangedListener { editable ->
				if (searchViewModel.searchState.value == SearchViewModel.SeachState.Query) {
					searchViewModel.setSearchQuery(editable.toString())
				}
			}
			it.editText.setOnEditorActionListener { _, _, _ ->
				onSubmitSearch()
				return@setOnEditorActionListener false
			}
			it.setOnMenuItemClickListener {
				startVoiceSearch()
				true
			}
			it.inflateMenu(R.menu.search)
		}

		launchOnCreated {
			searchViewModel.searchQuery.collectLatest {
				if (binding.searchView.text.toString() != it) {
					setSearchViewQueryAndFocus(it)
				}
			}
		}

		launchOnCreated {
			searchViewModel.searchState.drop(1).collectLatest { searchState ->
				val query = binding.searchView.text.toString()

				when (searchState) {
					SearchViewModel.SeachState.None -> {
						binding.searchView.hide()
						binding.searchbar.setText("")
					}

					SearchViewModel.SeachState.Query -> {
						binding.searchView.show()
						setSearchViewQueryAndFocus(query)
					}

					SearchViewModel.SeachState.Results -> {
						binding.searchView.hide()
						binding.searchbar.setText(query)

						navController.navigate(R.id.searchResultsFragment,
							bundleOf("title" to query),
							navOptions { launchSingleTop = true }
						)
					}
				}
			}
		}

		addMenuProvider(this)
		binding.searchbar.addMenuProvider(this)

		PreferenceManager.setDefaultValues(application, R.xml.preferences, false)

		if (savedInstanceState == null &&
			navController.currentDestination?.id == R.id.channelListFragment
		) {
			// new app start - explicitly navigate to start fragment defined in settings
			navController.navigate(startFragmentId)
		}

		requestPermissions()

		binding.appBar.applyHorizontalInsetsAsPadding()
		binding.navHostFragment.applyHorizontalInsetsAsPadding()

		ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.updateLayoutParams<AppBarLayout.LayoutParams> {
				topMargin = systemBars.top
			}
			insets
		}
	}

	private fun setSearchViewQueryAndFocus(query: String) {
		binding.searchView.setText(query)

		binding.searchView.editText.let {
			it.requestFocus()
			it.setSelection(query.length)

			// re-show onscreen keyboard
			val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
		}
	}

	@Suppress("UNUSED_PARAMETER")
	private fun onDestinationChanged(
		controller: NavController,
		destination: NavDestination,
		arguments: Bundle?
	) {
		// show bottom navigation for main destinations
		updateBottomNavigationVisibility()

		// show search for non destinations
		val showSearchBar = arguments?.getBoolean("show_search_bar", false) == true
		binding.searchbar.isVisible = showSearchBar
	}

	override fun onDestroy() {
		super.onDestroy()
		navController.removeOnDestinationChangedListener(::onDestinationChanged)
		_binding?.searchView?.removeTransitionListener(searchViewTransistionListener)
		_binding = null
	}

	fun addMenuProviderToSearchBar(
		provider: MenuProvider,
		owner: LifecycleOwner,
		state: Lifecycle.State
	) {
		binding.searchbar.addMenuProvider(provider, owner, state)
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

	private fun startVoiceSearch() {
		val voiceInputIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
			putExtra(
				RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
			)
		}
		voiceInputLauncher.launch(voiceInputIntent)
	}

	private fun onVoiceInputReceived(result: ActivityResult) {
		if (result.resultCode == RESULT_OK) {
			val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
				.let { it?.get(0) ?: "" }
			searchViewModel.setSearchQuery(spokenText)
			searchViewModel.submit()
		}
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
		val isMainDestination = arguments?.get("show_bottom_navigation")?.defaultValue == true

		val isSearchOverlayVisible =
			binding.searchView.currentTransitionState == SearchView.TransitionState.SHOWN ||
				binding.searchView.currentTransitionState == SearchView.TransitionState.SHOWING

		binding.bottomNavigation.isVisible = isMainDestination && !isSearchOverlayVisible
	}
}
