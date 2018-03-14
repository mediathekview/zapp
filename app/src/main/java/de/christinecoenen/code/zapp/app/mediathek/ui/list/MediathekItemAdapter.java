package de.christinecoenen.code.zapp.app.mediathek.ui.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;

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
		int layoutId = (viewType == VIEW_TYPE_ITEM) ?
			R.layout.fragment_mediathek_list_item : R.layout.fragment_mediathek_list_item_footer;

		View view = LayoutInflater
			.from(parent.getContext())
			.inflate(layoutId, parent, false);

		if (viewType == VIEW_TYPE_ITEM) {
			return new ItemViewHolder(view);
		} else {
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

		itemHodler.view.setOnClickListener(v -> {
			if (null != listener) {
				listener.onShowClicked(show);
			}
		});

		itemHodler.view.setOnLongClickListener(v -> {
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

		@BindView(R.id.title)
		protected TextView title;

		@BindView(R.id.topic)
		protected TextView topic;

		@BindView(R.id.text_show_duration)
		protected TextView duration;

		@BindView(R.id.text_show_channel)
		protected TextView channel;

		@BindView(R.id.text_show_time)
		protected TextView time;

		@BindView(R.id.text_show_subtitle)
		protected TextView subtitle;

		private final View view;

		ItemViewHolder(View view) {
			super(view);
			this.view = view;
			ButterKnife.bind(this, view);
		}

		void setShow(MediathekShow show) {
			title.setText(show.getTitle());
			topic.setText(show.getTopic());
			duration.setText(show.getFormattedDuration());
			channel.setText(show.getChannel());
			time.setText(show.getFormattedTimestamp());
			subtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);
		}

		@Override
		public String toString() {
			return super.toString() + " '" + title.getText() + "'";
		}
	}
}
