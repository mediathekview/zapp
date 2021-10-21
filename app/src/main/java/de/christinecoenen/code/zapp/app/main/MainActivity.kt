package de.christinecoenen.code.zapp.app.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.about.ui.AboutActivity
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity
import de.christinecoenen.code.zapp.databinding.ActivityMainBinding
import de.christinecoenen.code.zapp.utils.system.MenuHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

	private val viewModel: MainViewModel by viewModel()

	private var _binding: ActivityMainBinding? = null
	private val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		_binding = ActivityMainBinding.inflate(layoutInflater)

		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)

		binding.viewPager.adapter = MainPageAdapter(this, viewModel)
		binding.viewPager.isUserInputEnabled = false
		binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
			override fun onPageScrolled(
				position: Int,
				positionOffset: Float,
				positionOffsetPixels: Int
			) {
			}

			override fun onPageSelected(position: Int) {
				this@MainActivity.onPageSelected(position)
			}

			override fun onPageScrollStateChanged(state: Int) {}
		})

		binding.bottomNavigation.setOnItemSelectedListener(::onNavigationItemSelected)
		onPageSelected(binding.viewPager.currentItem)
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
		MenuHelper.uncheckItems(binding.bottomNavigation.menu)
		binding.bottomNavigation.menu.getItem(position).isChecked = true
	}

	private fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
		val page = viewModel.getPageTypeFromMenuResId(menuItem.itemId)
		binding.viewPager.setCurrentItem(page.ordinal, false)
		return true
	}
}
