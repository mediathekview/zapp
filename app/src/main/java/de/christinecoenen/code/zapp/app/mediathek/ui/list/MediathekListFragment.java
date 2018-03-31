package de.christinecoenen.code.zapp.app.mediathek.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.net.UnknownServiceException;
import java.util.Collections;

import javax.net.ssl.SSLHandshakeException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekService;
import de.christinecoenen.code.zapp.app.mediathek.api.request.query.Field;
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest;
import de.christinecoenen.code.zapp.app.mediathek.api.result.MediathekAnswer;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.MediathekDetailActivity;
import de.christinecoenen.code.zapp.utils.view.InfiniteScrollListener;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class MediathekListFragment extends Fragment implements MediathekItemAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

	private static final int ITEM_COUNT_PER_PAGE = 10;

	public static MediathekListFragment getInstance() {
		return new MediathekListFragment();
	}

	@BindView(R.id.list)
	protected RecyclerView recyclerView;

	@BindView(R.id.error)
	protected TextView errorView;

	@BindView(R.id.no_shows)
	protected View noShowsWarning;

	@BindView(R.id.refresh_layout)
	protected SwipeRefreshLayout swipeRefreshLayout;

	private MediathekService service;
	private Call<MediathekAnswer> getShowsCall;
	private QueryRequest queryRequest;
	private MediathekItemAdapter adapter;
	private InfiniteScrollListener scrollListener;
	private MediathekShow longClickShow;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MediathekListFragment() {
	}

	public void search(String query) {
		queryRequest.setSimpleSearch(query);
		loadItems(0, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		queryRequest = new QueryRequest()
			.setSize(ITEM_COUNT_PER_PAGE)
			.setSortAscending(Field.TIMESTAMP, Field.DURATION);

		// workaround to avoid SSLHandshakeException on Android 7 devices
		// see: https://stackoverflow.com/questions/39133437/sslhandshakeexception-handshake-failed-on-android-n-7-0
		ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
			.tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
			.build();

		HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

		OkHttpClient client = new OkHttpClient.Builder()
			.connectionSpecs(Collections.singletonList(spec))
			.addInterceptor(loggingInterceptor)
			.build();

		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("https://testing.mediathekviewweb.de/api/v2/")
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.build();

		service = retrofit.create(MediathekService.class);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mediathek_list, container, false);
		ButterKnife.bind(this, view);

		LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
		recyclerView.setLayoutManager(layoutManager);

		scrollListener = new InfiniteScrollListener(layoutManager) {
			@Override
			public void onLoadMore(int totalItemCount) {
				loadItems(totalItemCount, false);
			}
		};
		recyclerView.addOnScrollListener(scrollListener);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

		adapter = new MediathekItemAdapter(MediathekListFragment.this);
		recyclerView.setAdapter(adapter);

		loadItems(0, true);

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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_mediathek_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				onRefresh();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onShowClicked(MediathekShow show) {
		startActivity(MediathekDetailActivity.getStartIntent(getContext(), show));
	}

	@Override
	public void onShowLongClicked(MediathekShow show, View view) {
		this.longClickShow = show;
		PopupMenu menu = new PopupMenu(getContext(), view, Gravity.TOP | Gravity.END);
		menu.inflate(R.menu.activity_mediathek_detail);
		menu.show();
		menu.setOnMenuItemClickListener(this::onContextMenuItemClicked);
	}

	@Override
	public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
		loadItems(0, true);
	}

	private boolean onContextMenuItemClicked(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_share:
				startActivity(Intent.createChooser(longClickShow.getShareIntentPlain(), getString(R.string.action_share)));
				return true;
		}
		return false;
	}

	private void loadItems(int startWith, boolean replaceItems) {
		Timber.d("loadItems: %s", startWith);

		if (getShowsCall != null) {
			getShowsCall.cancel();
		}

		noShowsWarning.setVisibility(View.GONE);
		adapter.setLoading(true);

		queryRequest.setOffset(startWith);
		getShowsCall = service.listShows(queryRequest);
		getShowsCall.enqueue(new ShowCallResponseListener(replaceItems));
	}

	private void showError(int messageResId) {
		errorView.setText(messageResId);
		errorView.setVisibility(View.VISIBLE);
	}

	private class ShowCallResponseListener implements Callback<MediathekAnswer> {

		private final boolean replaceItems;

		ShowCallResponseListener(boolean replaceItems) {
			this.replaceItems = replaceItems;
		}

		@SuppressWarnings("ConstantConditions")
		@Override
		public void onResponse(@NonNull Call<MediathekAnswer> call, @NonNull Response<MediathekAnswer> response) {
			adapter.setLoading(false);
			scrollListener.setLoadingFinished();
			swipeRefreshLayout.setRefreshing(false);
			errorView.setVisibility(View.GONE);

			if (response.body() == null || response.body().result == null || response.body().err != null) {
				showError(R.string.error_mediathek_info_not_available);
				return;
			}

			if (replaceItems) {
				adapter.setShows(response.body().result.items);
			} else {
				adapter.addShows(response.body().result.items);
			}

			if (adapter.getItemCount() == 1) {
				noShowsWarning.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFailure(@NonNull Call<MediathekAnswer> call, @NonNull Throwable t) {
			adapter.setLoading(false);
			swipeRefreshLayout.setRefreshing(false);

			if (!call.isCanceled()) {
				// ignore canceled calls, because it most likely was canceled by app code
				Timber.e(t);

				if (t instanceof SSLHandshakeException || t instanceof UnknownServiceException) {
					showError(R.string.error_mediathek_ssl_error);
				} else {
					showError(R.string.error_mediathek_info_not_available);
				}
			}
		}

	}
}
