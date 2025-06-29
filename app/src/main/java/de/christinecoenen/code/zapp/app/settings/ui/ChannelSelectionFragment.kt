package de.christinecoenen.code.zapp.app.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableJsonChannelList
import de.christinecoenen.code.zapp.theme.ThemePreviews
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState


class ChannelSelectionFragment : Fragment(), MenuProvider {

	private lateinit var channelList: ISortableChannelList

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		channelList = SortableJsonChannelList(requireContext())
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		return ComposeView(requireContext())
			.apply {
				setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
				setContent { MainScreen() }
			}
	}

	override fun onPause() {
		super.onPause()

		channelList.persistChannelOrder()
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.channel_selection_fragment, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_help -> {
				openHelpDialog()
				true
			}

			else -> false
		}
	}

	@ThemePreviews
	@Composable
	fun MainScreen() {
		var list by remember { mutableStateOf(channelList.list) }
		val hapticFeedback = LocalHapticFeedback.current
		val lazyGridState = rememberLazyGridState()
		val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
			list = list.toMutableList().apply {
				add(to.index, removeAt(from.index))
			}

			channelList.replaceAllChannels(list)

			hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
		}

		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = 120.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			contentPadding = WindowInsets.safeDrawing
				.only(WindowInsetsSides.Bottom)
				.asPaddingValues(),
			state = lazyGridState,
			modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
		) {
			items(list, key = { it.id }) { channel ->
				ReorderableItem(reorderableLazyGridState, key = channel.id) { isDragging ->
					ChannelSelectionItem(channel, isDragging, this)
				}
			}
		}
	}

	private fun openHelpDialog() {
		val directions =
			ChannelSelectionFragmentDirections.toChannelSelectionHelpDialog()
		findNavController().navigate(directions)
	}
}
