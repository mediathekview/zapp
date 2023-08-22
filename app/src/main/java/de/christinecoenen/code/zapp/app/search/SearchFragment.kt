package de.christinecoenen.code.zapp.app.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.search.suggestions.LocalSearchSuggestionsAdapter
import de.christinecoenen.code.zapp.databinding.SearchFragmentBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SearchFragment : Fragment(), LocalSearchSuggestionsAdapter.Listener {

	private var _binding: SearchFragmentBinding? = null
	private val binding: SearchFragmentBinding get() = _binding!!

	private val viewModel: SearchViewModel by activityViewModel()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SearchFragmentBinding.inflate(inflater, container, false)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val lastQueriesAdapter =
			LocalSearchSuggestionsAdapter(this, R.drawable.ic_history_24)
		val localSuggestionsAdapter =
			LocalSearchSuggestionsAdapter(this, R.drawable.ic_baseline_search_24)
		binding.suggestions.adapter = ConcatAdapter(lastQueriesAdapter, localSuggestionsAdapter)

		viewLifecycleOwner.launchOnCreated {
			viewModel.lastQueries.collectLatest { pagingData ->
				lastQueriesAdapter.submitData(pagingData)
			}
		}

		viewLifecycleOwner.launchOnCreated {
			viewModel.localSearchSuggestions.collectLatest { pagingData ->
				localSuggestionsAdapter.submitData(pagingData)
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onSuggestionSelected(suggestion: String) {
		viewModel.setSearchQuery(suggestion)
		viewModel.submit()
	}

	override fun onSuggestionInserted(suggestion: String) {
		viewModel.setSearchQuery(suggestion)
	}
}
