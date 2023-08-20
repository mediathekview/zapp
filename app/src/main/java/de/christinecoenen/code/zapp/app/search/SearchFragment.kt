package de.christinecoenen.code.zapp.app.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.app.search.suggestions.LocalSearchSuggestionsAdapter
import de.christinecoenen.code.zapp.databinding.SearchFragmentBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SearchFragment : Fragment(), LocalSearchSuggestionsAdapter.Listener {

	private var _binding: SearchFragmentBinding? = null
	private val binding: SearchFragmentBinding get() = _binding!!

	private val viewModel: SearchViewModel by activityViewModel()

	private lateinit var localSuggestionsAdapter: LocalSearchSuggestionsAdapter

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SearchFragmentBinding.inflate(inflater, container, false)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// TODO: also display recent search queries
		localSuggestionsAdapter = LocalSearchSuggestionsAdapter(this)
		binding.suggestions.adapter = localSuggestionsAdapter

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

	override fun onSuggestionClicked(suggestion: String) {
		viewModel.setSearchQuery(suggestion)
		viewModel.submit()
	}

}
