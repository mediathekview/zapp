package de.christinecoenen.code.zapp.app.personal.series

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.personal.adapter.ShowCollectionListAdapter
import de.christinecoenen.code.zapp.databinding.SeriesFragmentBinding
import de.christinecoenen.code.zapp.databinding.ViewNoShowsBinding
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyBottomInsetAsPadding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SeriesFragment : Fragment(), MenuProvider {

	private var _binding: SeriesFragmentBinding? = null
	private val binding: SeriesFragmentBinding get() = _binding!!

	private var _noShowsBinding: ViewNoShowsBinding? = null
	private val noShowsBinding: ViewNoShowsBinding get() = _noShowsBinding!!

	private val viewModel: SeriesViewModel by viewModel()

	private lateinit var adapter: ShowCollectionListAdapter

	private var lastMarkResult: CollectionMarkResult? = null

	private val collectionClickListener = object : ShowCollectionListAdapter.Listener {
		override fun onCollectionClicked(collection: ShowCollection) {
			navigateToCollection(collection)
		}

		override fun onCollectionMenuClicked(collection: ShowCollection, view: View) {
			showCollectionMenu(collection, view)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		adapter = ShowCollectionListAdapter(collectionClickListener)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = SeriesFragmentBinding.inflate(inflater, container, false)
		_noShowsBinding = ViewNoShowsBinding.bind(binding.root)

		noShowsBinding.text.setText(R.string.fragment_series_no_results)
		noShowsBinding.icon.setImageResource(R.drawable.ic_sharp_format_list_bulleted_24)

		binding.list.adapter = adapter
		binding.list.applyBottomInsetAsPadding()

		launchOnCreated {
			viewModel.collectionsFlow.collect { collections ->
				adapter.submitList(collections)
				_noShowsBinding?.group?.isVisible = collections.isEmpty()
			}
		}

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

		setFragmentResultListener(EditSeriesDialog.REQUEST_KEY_SAVE) { _, bundle ->
			viewModel.save(
				bundle.getInt(EditSeriesDialog.RESULT_ID),
				bundle.getString(EditSeriesDialog.RESULT_NAME).orEmpty(),
				bundle.getString(EditSeriesDialog.RESULT_QUERY).orEmpty(),
				bundle.getString(EditSeriesDialog.RESULT_EXCLUDE_TERMS).orEmpty(),
				bundle.getBoolean(EditSeriesDialog.RESULT_DETECT_EPISODES),
				bundle.getBoolean(EditSeriesDialog.RESULT_SORT_BY_EPISODES),
				bundle.getBoolean(EditSeriesDialog.RESULT_SORT_EPISODES_ASCENDING, true)
			)
		}

		viewModel.markResult.observe(viewLifecycleOwner) { result ->
			if (result == null || result == lastMarkResult) {
				return@observe
			}

			lastMarkResult = result
			showCollectionMarkResult(result)
		}
	}

	override fun onResume() {
		super.onResume()

		// keep the unwatched/total counters up to date
		viewModel.refreshCounts()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		_noShowsBinding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		// the series list is a main destination like the other tabs, so it shows the
		// settings menu as well
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
		menuInflater.inflate(R.menu.series_list, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_add_collection -> {
				EditSeriesDialog.newInstance(null).show(parentFragmentManager, null)
				true
			}

			else -> false
		}
	}

	private fun showCollectionMenu(collection: ShowCollection, view: View) {
		PopupMenu(requireContext(), view).apply {
			menuInflater.inflate(R.menu.series_item, menu)

			setOnMenuItemClickListener { item ->
				when (item.itemId) {
					R.id.menu_edit_collection -> {
						EditSeriesDialog.newInstance(collection)
							.show(parentFragmentManager, null)
						true
					}

					R.id.menu_mark_all_watched -> {
						viewModel.markAllAsWatched(collection)
						true
					}

					R.id.menu_mark_all_unwatched -> {
						viewModel.markAllAsUnwatched(collection)
						true
					}

					R.id.menu_delete_collection -> {
						confirmDelete(collection)
						true
					}

					else -> false
				}
			}

			show()
		}
	}

	private fun confirmDelete(collection: ShowCollection) {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.dialog_series_delete_title)
			.setMessage(getString(R.string.dialog_series_delete_text, collection.name))
			.setIcon(R.drawable.ic_baseline_delete_outline_24)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok) { _, _ ->
				viewModel.delete(collection)
			}
			.show()
	}

	private fun navigateToCollection(collection: ShowCollection) {
		val directions = SeriesFragmentDirections.toSeriesDetailFragment(collectionId = collection.id)
		findNavController().navigate(directions)
	}
}
