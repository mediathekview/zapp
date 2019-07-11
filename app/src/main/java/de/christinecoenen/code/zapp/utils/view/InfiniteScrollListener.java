package de.christinecoenen.code.zapp.utils.view;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

	// The minimum amount of items to have below your current scroll position before loading more.
	private static final int VISIBLE_THRESHOLD = 5;

	// True if we are still waiting for the last set of data to load.
	private boolean loading = true;

	private final LinearLayoutManager mLinearLayoutManager;

	protected InfiniteScrollListener(LinearLayoutManager linearLayoutManager) {
		this.mLinearLayoutManager = linearLayoutManager;
	}

	public void setLoadingFinished() {
		loading = false;
	}

	@Override
	public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);

		int visibleItemCount = recyclerView.getChildCount();
		int totalItemCount = mLinearLayoutManager.getItemCount();
		int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

		if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
			// End has been reached

			onLoadMore(totalItemCount);
			loading = true;
		}
	}

	protected abstract void onLoadMore(int totalItemCount);
}
