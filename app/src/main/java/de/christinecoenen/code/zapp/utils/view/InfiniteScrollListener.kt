package de.christinecoenen.code.zapp.utils.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class InfiniteScrollListener protected constructor(
	private val mLinearLayoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {

	companion object {

		// The minimum amount of items to have below your current scroll position before loading more.
		private const val VISIBLE_THRESHOLD = 5

	}

	// True if we are still waiting for the last set of data to load.
	private var loading = true

	fun setLoadingFinished() {
		loading = false
	}

	override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
		super.onScrolled(recyclerView, dx, dy)
		
		val visibleItemCount = recyclerView.childCount
		val totalItemCount = mLinearLayoutManager.itemCount
		val firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition()

		if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD) {
			// End has been reached
			onLoadMore(totalItemCount)
			loading = true
		}
	}

	protected abstract fun onLoadMore(totalItemCount: Int)
}
