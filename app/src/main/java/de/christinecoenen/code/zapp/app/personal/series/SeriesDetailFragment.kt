package de.christinecoenen.code.zapp.app.personal.series

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelper
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.PagedMediathekShowListAdapter
import de.christinecoenen.code.zapp.databinding.SeriesDetailFragmentBinding
import de.christinecoenen.code.zapp.databinding.ViewNoShowsBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyBottomInsetAsPadding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Shows the mediathek search results of a single [de.christinecoenen.code.zapp.models.collections.ShowCollection].
 */
class SeriesDetailFragment : Fragment(), MenuProvider, MediathekShowListItemListener {

	private var _binding: SeriesDetailFragmentBinding? = null
	private val binding: SeriesDetailFragmentBinding get() = _binding!!

	private var _noShowsBinding: ViewNoShowsBinding? = null
	private val noShowsBinding: ViewNoShowsBinding get() = _noShowsBinding!!

	private val args: SeriesDetailFragmentArgs by navArgs()

	private val viewModel: SeriesDetailViewModel by viewModel {
		parametersOf(args.collectionId)
	}

	private lateinit var showAdapter: PagedMediathekShowListAdapter

	private var lastMarkResult: CollectionMarkResult? = null

	private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			updateNoShowsVisibility()
		}

		override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
			updateNoShowsVisibility()
		}

		override fun onStateRestorationPolicyChanged() {
			updateNoShowsVisibility()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		showAdapter = PagedMediathekShowListAdapter(lifecycleScope, false, this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SeriesDetailFragmentBinding.inflate(inflater, container, false)
		_noShowsBinding = ViewNoShowsBinding.bind(binding.root)

		noShowsBinding.text.setText(R.string.fragment_mediathek_no_results)
		noShowsBinding.icon.setImageResource(R.drawable.ic_sharp_format_list_bulleted_24)
		noShowsBinding.group.isVisible = false

		binding.list.adapter = showAdapter
		binding.list.applyBottomInsetAsPadding()

		showAdapter.registerAdapterDataObserver(adapterDataObserver)

		viewModel.showList.observe(viewLifecycleOwner) {
			launchOnCreated {
				showAdapter.submitData(it)
			}
		}

		viewModel.collection.observe(viewLifecycleOwner) { collection ->
			if (collection != null) {
				requireActivity().title = collection.name
			}
		}

		viewModel.markResult.observe(viewLifecycleOwner) { result ->
			if (result == null || result == lastMarkResult) {
				return@observe
			}

			lastMarkResult = result
			showCollectionMarkResult(result)
		}

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		showAdapter.unregisterAdapterDataObserver(adapterDataObserver)
		_binding = null
		_noShowsBinding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.series_detail, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_mark_all_watched -> {
				viewModel.markAllAsWatched()
				true
			}

			R.id.menu_mark_all_unwatched -> {
				viewModel.markAllAsUnwatched()
				true
			}

			else -> false
		}
	}

	override fun onShowClicked(show: MediathekShow) {
		val directions = SeriesDetailFragmentDirections.toMediathekDetailFragment(mediathekShow = show)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		ShowMenuHelper(this, show).apply {
			showContextMenu(view)
		}
	}

	private fun updateNoShowsVisibility() {
		_noShowsBinding?.group?.isVisible = showAdapter.itemCount == 0
	}
}
