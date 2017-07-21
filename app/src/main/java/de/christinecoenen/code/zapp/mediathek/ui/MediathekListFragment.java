package de.christinecoenen.code.zapp.mediathek.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.mediathek.api.MediathekAnswer;
import de.christinecoenen.code.zapp.mediathek.api.MediathekService;
import de.christinecoenen.code.zapp.mediathek.api.QueryRequest;
import de.christinecoenen.code.zapp.model.MediathekShow;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MediathekListFragment extends Fragment implements MediathekItemAdapter.Listener {

	public static MediathekListFragment getInstance() {
		return new MediathekListFragment();
	}

	@BindView(R.id.list)
	protected RecyclerView recyclerView;

	private MediathekService service;
	private Call<MediathekAnswer> getShowsCall;
	private QueryRequest queryRequest;

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
			.setSize(20)
			.addQuery("channel", "hr");

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

		recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

		getShowsCall = service.listShows(queryRequest);
		getShowsCall.enqueue(new ShowCallResponseListener());

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

	private class ShowCallResponseListener implements Callback<MediathekAnswer> {

		@SuppressWarnings("ConstantConditions")
		@Override
		public void onResponse(Call<MediathekAnswer> call, Response<MediathekAnswer> response) {
			if (response.body() == null || response.body().result == null) {
				Toast.makeText(getContext(), "No response", Toast.LENGTH_SHORT).show();
			} else {
				RecyclerView.Adapter adapter = new MediathekItemAdapter(
					response.body().result.results,
					MediathekListFragment.this);
				recyclerView.setAdapter(adapter);
			}
		}

		@Override
		public void onFailure(Call<MediathekAnswer> call, Throwable t) {
			Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}
}
