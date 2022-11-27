package de.christinecoenen.code.zapp.app.personal.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelper
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragmentDirections
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.PagedMediathekShowListAdapter
import de.christinecoenen.code.zapp.databinding.PersonalDetailsFragmentBinding
import de.christinecoenen.code.zapp.databinding.ViewNoShowsBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow

abstract class DetailsBaseFragment : Fragment(), MediathekShowListItemListener {

	private var _binding: PersonalDetailsFragmentBinding? = null
	private val binding: PersonalDetailsFragmentBinding get() = _binding!!

	private var _noShowsBinding: ViewNoShowsBinding? = null
	private val noShowsBinding: ViewNoShowsBinding get() = _noShowsBinding!!

	protected abstract val viewModel: DetailsBaseViewModel
	protected abstract val noShowsStringResId: Int
	protected abstract val noShowsIconResId: Int
	protected abstract val searchQueryHintResId: Int

	private lateinit var showAdapter: PagedMediathekShowListAdapter

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
		_binding = PersonalDetailsFragmentBinding.inflate(inflater, container, false)
		_noShowsBinding = ViewNoShowsBinding.bind(binding.root)

		noShowsBinding.text.setText(noShowsStringResId)
		noShowsBinding.icon.setImageResource(noShowsIconResId)
		binding.search.setHint(searchQueryHintResId)

		binding.list.adapter = showAdapter

		showAdapter.registerAdapterDataObserver(adapterDataObserver)

		binding.search.addTextChangedListener { editable ->
			viewModel.setSearchQueryFilter(editable.toString())
		}

		viewModel.showList.observe(viewLifecycleOwner) {
			lifecycleScope.launchWhenCreated {
				showAdapter.submitData(it)
			}
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		showAdapter.unregisterAdapterDataObserver(adapterDataObserver)
		_binding = null
		_noShowsBinding = null
	}

	override fun onShowClicked(show: MediathekShow) {
		val directions =
			MediathekListFragmentDirections.toMediathekDetailFragment(show)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		ShowMenuHelper(this, show).apply {
			showContextMenu(view)
		}
	}

	private fun updateNoShowsVisibility() {
		noShowsBinding.group.isVisible = showAdapter.itemCount == 0
	}
}
