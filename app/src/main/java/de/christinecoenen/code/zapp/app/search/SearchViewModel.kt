package de.christinecoenen.code.zapp.app.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	private val _isSubmitted = MutableStateFlow(false)
	val isSubmitted = _isSubmitted.asStateFlow()

	fun setSearchQuery(query: String?) {
		_isSubmitted.tryEmit(false)
		_searchQuery.tryEmit(query ?: "")
	}

	fun submit() {
		_isSubmitted.tryEmit(true)
		// TODO: save current query
	}
}
