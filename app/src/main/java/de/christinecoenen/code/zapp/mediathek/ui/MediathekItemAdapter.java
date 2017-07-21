package de.christinecoenen.code.zapp.mediathek.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.MediathekShow;

class MediathekItemAdapter extends RecyclerView.Adapter<MediathekItemAdapter.ViewHolder> {

	private final List<MediathekShow> shows;
	private final Listener listener;

	MediathekItemAdapter(List<MediathekShow> shows, Listener listener) {
		this.shows = shows;
		this.listener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater
			.from(parent.getContext())
			.inflate(R.layout.fragment_mediathek_list_item, parent, false);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		final MediathekShow show = shows.get(position);
		holder.setShow(show);

		holder.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != listener) {
					listener.onShowClicked(show);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return shows.size();
	}


	interface Listener {
		void onShowClicked(MediathekShow show);
	}


	static class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.title)
		protected TextView title;

		@BindView(R.id.topic)
		protected TextView topic;

		private final View view;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			ButterKnife.bind(this, view);
		}

		void setShow(MediathekShow show) {
			title.setText(show.getTitle());
			topic.setText(show.getTopic());
		}

		@Override
		public String toString() {
			return super.toString() + " '" + title.getText() + "'";
		}
	}
}
