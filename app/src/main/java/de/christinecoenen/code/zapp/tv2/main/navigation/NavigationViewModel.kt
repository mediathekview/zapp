package de.christinecoenen.code.zapp.tv2.main.navigation

import androidx.lifecycle.ViewModel
import de.christinecoenen.code.zapp.tv2.about.AboutScreenLocation
import de.christinecoenen.code.zapp.tv2.mediathek.MediaCenterScreenLocation
import de.christinecoenen.code.zapp.tv2.live.LiveScreenLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import org.acra.ktx.plus

class NavigationViewModel : ViewModel() {

    companion object {
        val MainTabLocations = listOf(
            LiveScreenLocation(),
            MediaCenterScreenLocation(),
            AboutScreenLocation(),
        )
    }

    private val _backstack = MutableStateFlow(listOf(MainTabLocations.first()))

    val currentLocation = _backstack.map { it.last() }

    val mainTabTitleResIds
        get() = MainTabLocations.map { it.titleResId!! }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSelectedTabIndex = currentLocation
        .mapLatest { MainTabLocations.indexOf(it) }

    fun selectMainTab(index: Int) {
        // main tabs always clear back stack
        _backstack.value = listOf(MainTabLocations[index])
    }

    fun <T : Location> showScreen(arguments: T) {
        _backstack.value += arguments
    }

    fun closeCurrentScreen() {
        _backstack.value = _backstack.value.subList(0, _backstack.value.lastIndex)
    }
}
