package de.christinecoenen.code.zapp.app.mediathek.ui.list

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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.main.MainActivity
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuHelper
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekLoadStateAdapter
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekShowListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.PagedMediathekShowListAdapter
import de.christinecoenen.code.zapp.databinding.MediathekListFragmentBinding
import de.christinecoenen.code.zapp.databinding.ViewNoShowsBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException


class MediathekListFragment : Fragment(),
	MenuProvider,
	MediathekShowListItemListener,
	OnRefreshListener {

	private var _binding: MediathekListFragmentBinding? = null
	private val binding: MediathekListFragmentBinding get() = _binding!!

	private var _noShowsBinding: ViewNoShowsBinding? = null
	private val noShowsBinding: ViewNoShowsBinding get() = _noShowsBinding!!

	private val viewmodel: MediathekListFragmentViewModel by viewModel {
		parametersOf(MutableStateFlow(""))
	}

	private lateinit var adapter: PagedMediathekShowListAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = MediathekListFragmentBinding.inflate(inflater, container, false)
		_noShowsBinding = ViewNoShowsBinding.bind(binding.root)

		val layoutManager = LinearLayoutManager(binding.root.context)
		binding.list.layoutManager = layoutManager

		binding.refreshLayout.setOnRefreshListener(this)
		binding.refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)

		(requireActivity() as MainActivity).addMenuProviderToSearchBar(
			this,
			viewLifecycleOwner,
			Lifecycle.State.RESUMED
		)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		adapter = PagedMediathekShowListAdapter(
			lifecycleScope,
			true,
			this@MediathekListFragment
		)

		binding.list.adapter = adapter.withLoadStateFooter(MediathekLoadStateAdapter(retry = adapter::retry))

		viewLifecycleOwner.launchOnCreated {
			viewmodel.pageFlow.collectLatest { pagingData ->
				adapter.submitData(pagingData)
			}
		}

		viewLifecycleOwner.launchOnCreated {
			viewmodel
				.pageFlow
				.drop(1)
				.collectLatest {
					// When new paging data was created we need to scroll up, because most
					// likely the filter parameters changed.
					// We skip the first item here, to not jump upong view recreation.
					binding.list.scrollToPosition(0)
				}
		}

		viewLifecycleOwner.launchOnCreated {
			adapter.loadStateFlow
				.map { it.refresh }
				.distinctUntilChanged()
				.collectLatest { refreshState ->
					binding.refreshLayout.isRefreshing = refreshState is LoadState.Loading
					binding.error.isVisible = refreshState is LoadState.Error
					updateNoShowsMessage(refreshState)

					when (refreshState) {
						is LoadState.Error -> onMediathekLoadErrorChanged(refreshState.error)
						is LoadState.NotLoading, LoadState.Loading -> Unit
					}
				}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		_noShowsBinding = null
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
		menuInflater.inflate(R.menu.mediathek_list_fragment, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {

			R.id.menu_refresh -> {
				onRefresh()
				true
			}

			else -> false
		}
	}

	override fun onShowClicked(show: MediathekShow) {
		val directions = MediathekListFragmentDirections.toMediathekDetailFragment(show)
		findNavController().navigate(directions)
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		ShowMenuHelper(this, show).apply {
			showContextMenu(view)
		}
	}

	override fun onRefresh() {
		adapter.refresh()
	}

	private fun onMediathekLoadErrorChanged(e: Throwable) {
		if (e is SSLHandshakeException || e is UnknownServiceException) {
			showError(R.string.error_mediathek_ssl_error)
		} else {
			showError(R.string.error_mediathek_info_not_available)
		}
	}

	private fun showError(messageResId: Int) {
		binding.error.setText(messageResId)
		binding.error.visibility = View.VISIBLE
	}

	private fun updateNoShowsMessage(loadState: LoadState) {
		val isAdapterEmpty = adapter.itemCount == 0 && loadState is LoadState.NotLoading
		noShowsBinding.group.isVisible = isAdapterEmpty
	}
}
