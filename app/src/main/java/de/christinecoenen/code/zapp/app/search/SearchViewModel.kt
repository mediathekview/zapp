package de.christinecoenen.code.zapp.app.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	fun setSearchQuery(query: String?) {
		_searchQuery.tryEmit(query ?: "")
	}
}
