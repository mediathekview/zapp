package de.christinecoenen.code.zapp.mediathek.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.mediathek.api.MediathekAnswer;
import de.christinecoenen.code.zapp.mediathek.api.MediathekService;
import de.christinecoenen.code.zapp.mediathek.api.QueryRequest;
import de.christinecoenen.code.zapp.model.MediathekShow;
import de.christinecoenen.code.zapp.utils.view.InfiniteScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MediathekListFragment extends Fragment implements MediathekItemAdapter.Listener {

	private static final String TAG = MediathekListFragment.class.getSimpleName();
	private static final int ITEM_COUNT_PER_PAGE = 10;

	public static MediathekListFragment getInstance() {
		return new MediathekListFragment();
	}

	@BindView(R.id.list)
	protected RecyclerView recyclerView;

	@BindView(R.id.error)
	protected TextView errorView;

	private MediathekService service;
	private Call<MediathekAnswer> getShowsCall;
	private QueryRequest queryRequest;
	private MediathekItemAdapter adapter;
	private InfiniteScrollListener scrollListener;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MediathekListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		queryRequest = new QueryRequest()
			.setSize(ITEM_COUNT_PER_PAGE);

		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("https://mediathekviewweb.de/api/")
			.addConverterFactory(GsonConverterFactory.create())
			.build();

		service = retrofit.create(MediathekService.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mediathek_list, container, false);
		ButterKnife.bind(this, view);

		LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
		recyclerView.setLayoutManager(layoutManager);

		scrollListener = new InfiniteScrollListener(layoutManager) {
			@Override
			public void onLoadMore(int totalItemCount) {
				loadItems(totalItemCount);
			}
		};
		recyclerView.addOnScrollListener(scrollListener);

		adapter = new MediathekItemAdapter(MediathekListFragment.this);
		recyclerView.setAdapter(adapter);

		loadItems(0);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (getShowsCall != null) {
			getShowsCall.cancel();
		}
	}

	@Override
	public void onShowClicked(MediathekShow show) {
		((MediathekFragment) getParentFragment())
			.navigateTo(MediathekDetailFragment.getInstance(show), show.getId());
	}

	private void loadItems(int startWith) {
		Log.d(TAG, "loadItems: " + startWith);

		if (getShowsCall != null) {
			getShowsCall.cancel();
		}

		adapter.setLoading(true);

		queryRequest.setOffset(startWith);
		getShowsCall = service.listShows(queryRequest);
		getShowsCall.enqueue(new ShowCallResponseListener());
	}

	private void onMediathekApiError() {
		showError(R.string.error_mediathek_info_not_available);
		scrollListener.setLoadingFailed();
	}

	private void showError(int messageResId) {
		errorView.setText(messageResId);
		errorView.setVisibility(View.VISIBLE);
	}

	private class ShowCallResponseListener implements Callback<MediathekAnswer> {

		@SuppressWarnings("ConstantConditions")
		@Override
		public void onResponse(Call<MediathekAnswer> call, Response<MediathekAnswer> response) {
			adapter.setLoading(false);

			if (response.body() == null || response.body().result == null) {
				onMediathekApiError();
			} else {
				if (response.body().err == null) {
					adapter.add(response.body().result.results);
					errorView.setVisibility(View.GONE);
				} else {
					onMediathekApiError();
				}
			}
		}

		@Override
		public void onFailure(Call<MediathekAnswer> call, Throwable t) {
			adapter.setLoading(false);

			if (!call.isCanceled()) {
				// ignore canceled calls, because it most likely was canceled by app code
				Log.e(TAG, t.toString());
				onMediathekApiError();
			}
		}

	}
}
