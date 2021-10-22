package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity.Companion.getStartIntent
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.MediathekItemAdapter
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.view.InfiniteScrollListener
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException

class MediathekListFragment : Fragment(), ListItemListener, OnRefreshListener {

	companion object {

		private const val ITEM_COUNT_PER_PAGE = 30

		val instance
			get() = MediathekListFragment()

	}

	private var _binding: FragmentMediathekListBinding? = null
	private val binding: FragmentMediathekListBinding
		get() = _binding!!

	private val mediathekRepository: MediathekRepository by inject()

	private var queryRequest = QueryRequest()
	private var adapter: MediathekItemAdapter? = null
	private var scrollListener: InfiniteScrollListener? = null
	private var longClickShow: MediathekShow? = null
	private var getShowsJob: Job? = null

	fun search(query: String?) {
		queryRequest.setSimpleSearch(query)
		loadItems(0, true)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setHasOptionsMenu(true)

		queryRequest = QueryRequest()
		queryRequest.size = ITEM_COUNT_PER_PAGE
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
				loadItems(totalItemCount, false)
			}
		}

		// TODO: debounce
		binding.filter.search.addTextChangedListener { editable -> search(editable.toString()) }
		binding.list.addOnScrollListener(scrollListener!!)
		binding.refreshLayout.setOnRefreshListener(this)
		binding.refreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)

		adapter = MediathekItemAdapter(this@MediathekListFragment)
		binding.list.adapter = adapter

		loadItems(0, true)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
		getShowsJob?.cancel()
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
		binding.refreshLayout.isRefreshing = true
		loadItems(0, true)
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

	private fun loadItems(startWith: Int, replaceItems: Boolean) {
		Timber.d("loadItems: %s", startWith)

		getShowsJob?.cancel()

		binding.noShows.visibility = View.GONE
		adapter?.setLoading(true)

		queryRequest.offset = startWith

		getShowsJob = lifecycleScope.launchWhenCreated {
			try {
				val shows = mediathekRepository.listShows(queryRequest)
				onMediathekLoadSuccess(shows, replaceItems)
			} catch (e: Exception) {
				onMediathekLoadError(e)
			}
		}
	}

	private fun showError(messageResId: Int) {
		binding.error.setText(messageResId)
		binding.error.visibility = View.VISIBLE
	}

	private fun onMediathekLoadSuccess(shows: List<MediathekShow>, replaceItems: Boolean) {
		adapter?.setLoading(false)
		scrollListener?.setLoadingFinished()

		binding.refreshLayout.isRefreshing = false
		binding.error.visibility = View.GONE

		if (replaceItems) {
			adapter?.setShows(shows)
		} else {
			adapter?.addShows(shows)
		}

		if (adapter?.itemCount == 1) {
			binding.noShows.visibility = View.VISIBLE
		}
	}

	private fun onMediathekLoadError(e: Throwable) {
		adapter?.setLoading(false)
		binding.refreshLayout.isRefreshing = false

		Timber.e(e)

		if (e is SSLHandshakeException || e is UnknownServiceException) {
			showError(R.string.error_mediathek_ssl_error)
		} else {
			showError(R.string.error_mediathek_info_not_available)
		}
	}
}
