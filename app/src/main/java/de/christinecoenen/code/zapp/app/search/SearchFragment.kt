package de.christinecoenen.code.zapp.app.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.app.search.suggestions.LocalSearchSuggestionsAdapter
import de.christinecoenen.code.zapp.databinding.SearchFragmentBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnResumed
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SearchFragment : Fragment() {

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

		launchOnResumed {
			viewModel.isSubmitted.collectLatest { isSubmitted ->
				binding.suggestions.isVisible = !isSubmitted
				binding.results.isVisible = isSubmitted

				if (isSubmitted) {
					binding.results.text = "Results for " + viewModel.searchQuery.value
				}
			}
		}

		// TODO: also display recent search queries
		localSuggestionsAdapter = LocalSearchSuggestionsAdapter()
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

}
