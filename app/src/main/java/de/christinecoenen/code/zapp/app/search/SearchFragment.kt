package de.christinecoenen.code.zapp.app.search

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChannelChipContent
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChipType
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChipsAdapter
import de.christinecoenen.code.zapp.app.search.suggestions.chips.SuggestionChipListener
import de.christinecoenen.code.zapp.app.search.suggestions.text.LocalSearchSuggestionsAdapter
import de.christinecoenen.code.zapp.app.search.suggestions.text.SuggestionTextListener
import de.christinecoenen.code.zapp.app.search.suggestions.text.TextSuggestionType
import de.christinecoenen.code.zapp.databinding.SearchFragmentBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SearchFragment : Fragment(), SuggestionTextListener {

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

		// chips
		val selectedChannelsChipsAdapter = ChipsAdapter(
			ChipType.InteractableFilter,
			object : SuggestionChipListener<ChannelChipContent> {
				override fun onChipClick(content: ChannelChipContent) {
					viewModel.removeChannel(content.channel)
				}
			})
		val suggestedChannelsChipsAdapter = ChipsAdapter(
			ChipType.Suggestion,
			object : SuggestionChipListener<ChannelChipContent> {
				override fun onChipClick(content: ChannelChipContent) {
					viewModel.addChannel(content.channel)
				}
			})
		binding.chips.adapter = ConcatAdapter(
			selectedChannelsChipsAdapter,
			suggestedChannelsChipsAdapter
		)

		viewLifecycleOwner.launchOnCreated {
			viewModel.channels.collectLatest { channels ->
				selectedChannelsChipsAdapter.submitList(channels.map {
					ChannelChipContent(it)
				})
			}
		}
		viewLifecycleOwner.launchOnCreated {
			viewModel.channelSuggestions.collectLatest { channelSuggestions ->
				suggestedChannelsChipsAdapter.submitList(channelSuggestions.map {
					ChannelChipContent(it)
				})
			}
		}

		// suggestions
		val lastQueriesAdapter =
			LocalSearchSuggestionsAdapter(this, TextSuggestionType.RecentQuery)
		val localSuggestionsAdapter =
			LocalSearchSuggestionsAdapter(this, TextSuggestionType.Visited)
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

	override fun onSuggestionLongPress(
		suggestion: String,
		type: TextSuggestionType,
		view: View
	): Boolean {
		if (type != TextSuggestionType.RecentQuery) {
			return false
		}

		val menu = PopupMenu(context, view, Gravity.TOP or Gravity.END)
		menu.inflate(R.menu.search_recent_query_context)
		menu.show()
		menu.setOnMenuItemClickListener { menuItem ->
			onSuggesionContextMenuClicked(menuItem, suggestion)
		}

		return true
	}

	private fun onSuggesionContextMenuClicked(menuItem: MenuItem, suggestion: String): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_delete -> {
				viewModel.deleteSavedSearchQuery(suggestion)
				true
			}

			else -> false
		}
	}
}
