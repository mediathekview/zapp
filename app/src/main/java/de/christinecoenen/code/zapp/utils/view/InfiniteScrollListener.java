package de.christinecoenen.code.zapp.utils.view;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

	// The minimum amount of items to have below your current scroll position before loading more.
	private static final int VISIBLE_THRESHOLD = 5;

	// The total number of items in the dataset after the last load
	private int previousTotal = 0;

	// True if we are still waiting for the last set of data to load.
	private boolean loading = true;

	private final LinearLayoutManager mLinearLayoutManager;

	protected InfiniteScrollListener(LinearLayoutManager linearLayoutManager) {
		this.mLinearLayoutManager = linearLayoutManager;
	}

	public void setLoadingFailed() {
		loading = false;
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);

		int visibleItemCount = recyclerView.getChildCount();
		int totalItemCount = mLinearLayoutManager.getItemCount();
		int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
			}
		}

		if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
			// End has been reached

			onLoadMore(totalItemCount);
			loading = true;
		}
	}

	public abstract void onLoadMore(int totalItemCount);
}
