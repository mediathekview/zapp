package de.christinecoenen.code.zapp.tv2.main

import androidx.compose.runtime.IntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.R

class TopNavigationViewModel : ViewModel() {

	val tabsStringIds = listOf(
		R.string.activity_main_tab_live,
		R.string.activity_main_tab_mediathek,
		R.string.menu_about_short,
	)

	private val _selectedTab = mutableIntStateOf(0)
	val selectedTab: IntState = _selectedTab

	fun isSelected(index: Int): Boolean {
		return index == _selectedTab.intValue
	}

	fun select(index: Int) {
		_selectedTab.intValue = index
	}
}
