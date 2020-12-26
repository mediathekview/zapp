package de.christinecoenen.code.zapp.app.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.about.ui.AboutActivity
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekSearchSuggestionsProvider
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragment
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity
import de.christinecoenen.code.zapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

	private val viewModel: MainViewModel by viewModels()

	private var _binding: ActivityMainBinding? = null
	private val binding get() = _binding!!

	private var searchQuery: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		_binding = ActivityMainBinding.inflate(layoutInflater)

		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)

		binding.viewPager.adapter = MainPageAdapter(this, viewModel)
		binding.viewPager.isUserInputEnabled = false
		binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
			override fun onPageSelected(position: Int) {
				this@MainActivity.onPageSelected(position)
			}

			override fun onPageScrollStateChanged(state: Int) {}
		})

		val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
		binding.search.setOnQueryTextListener(this)
		binding.search.setIconifiedByDefault(false)
		binding.search.setSearchableInfo(searchManager.getSearchableInfo(componentName))
		binding.search.clearFocus()
		binding.search.setOnQueryTextFocusChangeListener(::onSearchQueryTextFocusChangeListener)

		binding.bottomNavigation.setOnNavigationItemSelectedListener(::onNavigationItemSelected)
		onPageSelected(binding.viewPager.currentItem)
		handleIntent(intent)
	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		handleIntent(intent)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString(ARG_QUERY, searchQuery)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)

		searchQuery = savedInstanceState.getString(ARG_QUERY)
		search(searchQuery)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_about -> {
				val intent = AboutActivity.getStartIntent(this)
				startActivity(intent)
				true
			}
			R.id.menu_settings -> {
				val intent = SettingsActivity.getStartIntent(this)
				startActivity(intent)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun onPageSelected(position: Int) {
		binding.search.clearFocus()

		PageType.values().forEach {
			binding.bottomNavigation.menu.getItem(it.ordinal).isChecked = false
		}
		binding.bottomNavigation.menu.getItem(position).isChecked = true

		val pageType = viewModel.getPageTypeAt(position)
		binding.search.isVisible = when (pageType) {
			PageType.PAGE_MEDIATHEK_LIST -> true
			else -> false
		}
	}

	private fun handleIntent(intent: Intent) {
		if (Intent.ACTION_SEARCH == intent.action) {
			// called by searchView on search commit
			val query = intent.getStringExtra(SearchManager.QUERY)

			binding.search.setOnQueryTextListener(null)
			binding.search.clearFocus()
			binding.search.setQuery(query, false)
			binding.search.setOnQueryTextListener(this)

			search(query)

			MediathekSearchSuggestionsProvider.saveQuery(this, query)
		}
	}

	private fun search(query: String?) {
		searchQuery = query

		val currentFragment = supportFragmentManager
			.findFragmentByTag("f" + binding.viewPager.currentItem)

		if (currentFragment is MediathekListFragment) {
			currentFragment.search(query)
		}
	}

	private fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
		val page = viewModel.getPageTypeFromMenuResId(menuItem.itemId)
		binding.viewPager.setCurrentItem(page.ordinal, false)
		return true
	}

	/*
	 * To open up the soft keyboard on Android (Fire) TV when focusing the SearchView.
	 */
	private fun onSearchQueryTextFocusChangeListener(searchView: View, hasFocus: Boolean) {
		if (hasFocus && !searchView.isInTouchMode) {
			searchView.post {
				val imm = this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
				imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_FORCED)
			}
		}
	}

	override fun onQueryTextSubmit(query: String): Boolean {
		return false
	}

	override fun onQueryTextChange(newText: String): Boolean {
		search(newText)
		return true
	}

	companion object {
		private const val ARG_QUERY = "ARG_QUERY"
	}
}
