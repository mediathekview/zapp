package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity.Companion.getStartIntent
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemAdapter
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.utils.view.InfiniteScrollListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException

class MediathekListFragment : Fragment(), ListItemListener, OnRefreshListener {

	companion object {

		val instance
			get() = MediathekListFragment()

	}

	private var _binding: FragmentMediathekListBinding? = null
	private val binding: FragmentMediathekListBinding
		get() = _binding!!

	private val viewmodel: MediathekListFragmentViewModel by viewModel()

	private var adapter: MediathekItemAdapter? = null
	private var scrollListener: InfiniteScrollListener? = null
	private var longClickShow: MediathekShow? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setHasOptionsMenu(true)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMediathekListBinding.inflate(inflater, container, false)

		val layoutManager = LinearLayoutManager(binding.root.context)
		binding.list.layoutManager = layoutManager

		scrollListener = object : InfiniteScrollListener(layoutManager) {
			public override fun onLoadMore(totalItemCount: Int) {
				viewmodel.loadItems(totalItemCount, false)
			}
		}

		binding.filter.search.addTextChangedListener { editable ->
			viewmodel.search(editable.toString())
		}
		binding.list.addOnScrollListener(scrollListener!!)
		binding.refreshLayout.setOnRefreshListener(this)
		binding.refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)

		adapter = MediathekItemAdapter(this@MediathekListFragment)
		binding.list.adapter = adapter

		viewmodel.isLoading.observe(viewLifecycleOwner, ::onIsLoadingChanged)
		viewmodel.mediathekLoadError.observe(viewLifecycleOwner, ::onMediathekLoadErrorChanged)
		viewmodel.mediathekLoadResult.observe(viewLifecycleOwner, ::onMediathekLoadResultChanged)

		viewmodel.loadItems(0, true)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.fragment_mediathek_list, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_refresh -> {
				onRefresh()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onShowClicked(show: MediathekShow) {
		startActivity(getStartIntent(context, show))
	}

	override fun onShowLongClicked(show: MediathekShow, view: View) {
		longClickShow = show

		PopupMenu(context, view, Gravity.TOP or Gravity.END).apply {
			inflate(R.menu.activity_mediathek_detail)
			show()
			setOnMenuItemClickListener(::onContextMenuItemClicked)
		}
	}

	override fun onRefresh() {
		viewmodel.loadItems(0, true)
	}

	private fun onContextMenuItemClicked(menuItem: MenuItem): Boolean {
		when (menuItem.itemId) {
			R.id.menu_share -> {
				startActivity(
					Intent.createChooser(
						longClickShow!!.shareIntentPlain,
						getString(R.string.action_share)
					)
				)
				return true
			}
		}
		return false
	}

	private fun onIsLoadingChanged(isLoading: Boolean) {
		adapter?.setLoading(isLoading)
		binding.refreshLayout.isRefreshing = isLoading

		if (!isLoading) {
			scrollListener?.setLoadingFinished()
		}
	}

	private fun onMediathekLoadResultChanged(mediathekLoadResult: MediathekListFragmentViewModel.MediathekLoadResult) {
		if (mediathekLoadResult.replaceItems) {
			adapter?.setShows(mediathekLoadResult.shows)
		} else {
			adapter?.addShows(mediathekLoadResult.shows)
		}

		binding.noShows.isVisible = adapter?.itemCount == 1
	}

	private fun onMediathekLoadErrorChanged(e: Throwable?) {
		if (e == null) {
			binding.error.visibility = View.GONE
			return
		}

		Timber.e(e)

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
}
