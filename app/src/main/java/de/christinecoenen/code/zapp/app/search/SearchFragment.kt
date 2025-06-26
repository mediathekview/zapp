package de.christinecoenen.code.zapp.app.search

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ConcatAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.search.dialogs.ConfirmDeleteSearchHistoryDialog
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChannelChipContent
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChipType
import de.christinecoenen.code.zapp.app.search.suggestions.chips.ChipsAdapter
import de.christinecoenen.code.zapp.app.search.suggestions.chips.DurationChipContent
import de.christinecoenen.code.zapp.app.search.suggestions.chips.SuggestionChipListener
import de.christinecoenen.code.zapp.app.search.suggestions.text.ClearSearchHistoryButtonAdapter
import de.christinecoenen.code.zapp.app.search.suggestions.text.LocalSearchSuggestionsAdapter
import de.christinecoenen.code.zapp.app.search.suggestions.text.LocalSearchSuggestionsHeaderAdapter
import de.christinecoenen.code.zapp.app.search.suggestions.text.SuggestionTextListener
import de.christinecoenen.code.zapp.app.search.suggestions.text.TextSuggestionType
import de.christinecoenen.code.zapp.databinding.SearchFragmentBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyBottomInsetAsPadding
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyHorizontalInsetsAsPadding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SearchFragment : Fragment(), SuggestionTextListener,
	ClearSearchHistoryButtonAdapter.Listener {

	private var _binding: SearchFragmentBinding? = null
	private val binding: SearchFragmentBinding get() = _binding!!

	private val viewModel: SearchViewModel by activityViewModel()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SearchFragmentBinding.inflate(inflater, container, false)

		binding.suggestions.applyBottomInsetAsPadding()
		binding.root.applyHorizontalInsetsAsPadding()

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
		val selectedDurationQueriesChipsAdapter = ChipsAdapter(
			ChipType.InteractableFilter,
			object : SuggestionChipListener<DurationChipContent> {
				override fun onChipClick(content: DurationChipContent) {
					viewModel.removeDurationQuery(content.durationQuery)
				}
			})
		val suggestedDurationChipsAdapter = ChipsAdapter(
			ChipType.Suggestion,
			object : SuggestionChipListener<DurationChipContent> {
				override fun onChipClick(content: DurationChipContent) {
					viewModel.addOrReplaceDurationQuery(content.durationQuery)
				}
			})
		binding.selectedChips.adapter = ConcatAdapter(
			selectedDurationQueriesChipsAdapter,
			selectedChannelsChipsAdapter,
		)
		binding.suggestedChips.adapter = ConcatAdapter(
			suggestedDurationChipsAdapter,
			suggestedChannelsChipsAdapter,
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
		viewLifecycleOwner.launchOnCreated {
			viewModel.durationQuerySet.collectLatest { durationQuerySet ->
				selectedDurationQueriesChipsAdapter.submitList(durationQuerySet.map {
					DurationChipContent(it)
				})
			}
		}
		viewLifecycleOwner.launchOnCreated {
			viewModel.durationSuggestionSet.collectLatest { durationSuggestionSet ->
				suggestedDurationChipsAdapter.submitList(durationSuggestionSet.map {
					DurationChipContent(it)
				})
			}
		}

		// filter help text
		viewLifecycleOwner.launchOnCreated {
			combine(
				viewModel.filterCount,
				viewModel.suggestionCount
			) { count1, count2 -> count1 + count2 }
				.collectLatest { chipCount ->
					binding.filterHelpText.isVisible = chipCount == 0
				}
		}

		// suggestions
		val lastQueriesHeaderAdapter =
			LocalSearchSuggestionsHeaderAdapter(R.string.search_suggestion_header_last_searches)
		val lastQueriesAdapter =
			LocalSearchSuggestionsAdapter(this, TextSuggestionType.RecentQuery)
		val clearSearchHistoryButtonAdapter = ClearSearchHistoryButtonAdapter(this)
		val localSuggestionsHeaderAdapter =
			LocalSearchSuggestionsHeaderAdapter(R.string.search_suggestion_header_local_suggestions)
		val localSuggestionsAdapter =
			LocalSearchSuggestionsAdapter(this, TextSuggestionType.Visited)
		binding.suggestions.adapter = ConcatAdapter(
			lastQueriesHeaderAdapter,
			lastQueriesAdapter,
			clearSearchHistoryButtonAdapter,
			localSuggestionsHeaderAdapter,
			localSuggestionsAdapter,
		)

		viewLifecycleOwner.launchOnCreated {
			viewModel.lastQueries.collectLatest { pagingData ->
				lastQueriesAdapter.submitData(pagingData)
			}
		}
		lastQueriesAdapter.addOnPagesUpdatedListener {
			lastQueriesHeaderAdapter.setIsVisible(lastQueriesAdapter.itemCount > 0)
			clearSearchHistoryButtonAdapter.setIsVisible(
				viewModel.searchQuery.value.isEmpty() && lastQueriesAdapter.itemCount > 0
			)
		}

		viewLifecycleOwner.launchOnCreated {
			viewModel.localSearchSuggestions.collectLatest { pagingData ->
				localSuggestionsAdapter.submitData(pagingData)
			}
		}
		localSuggestionsAdapter.addOnPagesUpdatedListener {
			localSuggestionsHeaderAdapter.setIsVisible(localSuggestionsAdapter.itemCount > 0)
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

	override fun onClearSearchHistoryClicked() {
		showConfirmDeleteAllHistoryDialog()
	}

	private fun showConfirmDeleteAllHistoryDialog() {
		val dialog = ConfirmDeleteSearchHistoryDialog()

		setFragmentResultListener(ConfirmDeleteSearchHistoryDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
			viewModel.deleteAllSavedSearchQueries()
		}

		dialog.show(parentFragmentManager, null)
	}
}
