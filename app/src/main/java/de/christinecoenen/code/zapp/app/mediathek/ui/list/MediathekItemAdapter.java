package de.christinecoenen.code.zapp.app.mediathek.ui.list;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.ZappApplication;
import de.christinecoenen.code.zapp.app.mediathek.model.DownloadStatus;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository;
import de.christinecoenen.code.zapp.databinding.FragmentMediathekListItemBinding;
import de.christinecoenen.code.zapp.utils.system.ImageHelper;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

class MediathekItemAdapter extends RecyclerView.Adapter<MediathekItemAdapter.ViewHolder> {

	private static final int VIEW_TYPE_ITEM = 0;
	private static final int VIEW_TYPE_FOOTER = 1;

	private List<MediathekShow> shows = new ArrayList<>();
	private final Listener listener;
	private ProgressBar progressBar;

	MediathekItemAdapter(Listener listener) {
		this.listener = listener;
		setHasStableIds(true);
	}

	void setShows(List<MediathekShow> shows) {
		if (!shows.equals(this.shows)) {
			this.shows = shows;
			notifyDataSetChanged();
		}
	}

	void addShows(List<MediathekShow> shows) {
		if (!shows.isEmpty()) {
			this.shows.addAll(shows);
			notifyDataSetChanged();
		}
	}

	void setLoading(boolean loading) {
		if (progressBar != null) {
			progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public long getItemId(int position) {
		if (position == shows.size()) {
			// fixed id for footer
			return 0;
		}
		return shows.get(position).hashCode();
	}

	@Override
	public int getItemViewType(int position) {
		return position == shows.size() ? VIEW_TYPE_FOOTER : VIEW_TYPE_ITEM;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());

		if (viewType == VIEW_TYPE_ITEM) {
			FragmentMediathekListItemBinding binding =
				FragmentMediathekListItemBinding.inflate(inflater, parent, false);
			return new ItemViewHolder(binding);
		} else {
			View view = inflater.inflate(R.layout.fragment_mediathek_list_item_footer, parent, false);
			progressBar = view.findViewById(R.id.progress);
			return new ViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
		if (position == shows.size()) {
			// nothing to do for footer
			return;
		}

		final MediathekShow show = shows.get(position);
		ItemViewHolder itemHodler = (ItemViewHolder) holder;
		itemHodler.setShow(show);

		itemHodler.getView().setOnClickListener(v -> {
			if (null != listener) {
				listener.onShowClicked(show);
			}
		});

		itemHodler.getView().setOnLongClickListener(v -> {
			if (null != listener) {
				listener.onShowLongClicked(show, v);
				return true;
			}
			return false;
		});
	}

	@Override
	public int getItemCount() {
		return shows.size() + 1;
	}


	interface Listener {
		void onShowClicked(MediathekShow show);

		void onShowLongClicked(MediathekShow show, View view);
	}


	static class ViewHolder extends RecyclerView.ViewHolder {
		ViewHolder(View view) {
			super(view);
		}
	}

	static class ItemViewHolder extends ViewHolder {

		private final FragmentMediathekListItemBinding binding;
		private final MediathekRepository mediathekRepository;
		private final CompositeDisposable disposables = new CompositeDisposable();

		ItemViewHolder(FragmentMediathekListItemBinding binding) {
			super(binding.getRoot());

			ZappApplication app = ((ZappApplication) binding.getRoot().getContext().getApplicationContext());
			mediathekRepository = app.getMediathekRepository();

			this.binding = binding;
		}

		View getView() {
			return binding.getRoot();
		}

		void setShow(MediathekShow show) {
			disposables.clear();

			binding.imageHolder.setVisibility(View.GONE);
			binding.thumbnail.setImageBitmap(null);
			binding.progress.setScaleX(0);
			binding.title.setText(show.getTitle());
			binding.topic.setText(show.getTopic());
			binding.duration.setText(show.getFormattedDuration());
			binding.channel.setText(show.getChannel());
			binding.time.setText(show.getFormattedTimestamp());
			binding.subtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);
			binding.subtitleDivider.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

			Flowable<PersistedMediathekShow> persistedShowCall = mediathekRepository
				.getPersistedShowByApiId(show.getApiId());

			Disposable isDownloadDisposable = persistedShowCall
				.filter(persistedShow -> persistedShow.getDownloadStatus() == DownloadStatus.COMPLETED)
				.firstOrError()
				.flatMap(persistedShow -> ImageHelper.loadThumbnailAsync(binding.getRoot().getContext(), persistedShow.getDownloadedVideoPath()))
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::updatethumbnail, Timber::e);

			Disposable viewingProgressDisposable = mediathekRepository
				.getPlaybackPositionPercent(show.getApiId())
				.filter(playbackPosition -> playbackPosition > 0)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::updatePlaybackPosition, Timber::e);

			disposables.add(isDownloadDisposable);
			disposables.add(viewingProgressDisposable);
		}

		private void updatePlaybackPosition(float progressPercent) {
			binding.progress.setScaleX(progressPercent);
			binding.imageHolder.setVisibility(View.VISIBLE);
		}

		private void updatethumbnail(Bitmap thumbnail) {
			binding.thumbnail.setImageBitmap(thumbnail);
			binding.imageHolder.setVisibility(View.VISIBLE);
		}

		@NonNull
		@Override
		public String toString() {
			return super.toString() + " '" + binding.title.getText() + "'";
		}
	}
}
