package de.christinecoenen.code.zapp.app.livestream.ui.list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.WeakHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.livestream.ui.views.ProgramInfoViewBase;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;

class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder> {

	@SuppressWarnings("unused")
	private static final String TAG = ChannelListAdapter.class.getSimpleName();

	private final IChannelList channelList;
	private final LayoutInflater inflater;
	private final WeakHashMap<ViewHolder, ?> visibleViews = new WeakHashMap<>();
	private final Listener listener;

	ChannelListAdapter(Context context, IChannelList channelList, Listener listener) {
		this.channelList = channelList;
		this.listener = listener;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setHasStableIds(true);
	}

	void pause() {
		for (ViewHolder holder : visibleViews.keySet()) {
			holder.pause();
		}
	}

	void resume() {
		notifyDataSetChanged();
		for (ViewHolder holder : visibleViews.keySet()) {
			holder.resume();
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.fragment_channel_list_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		ChannelModel channel = channelList.get(position);
		holder.setChannel(channel);
	}

	@Override
	public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
		holder.pause();
		visibleViews.remove(holder);
	}

	@Override
	public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
		holder.resume();
		visibleViews.put(holder, null);
	}

	@Override
	public int getItemCount() {
		return channelList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		@BindView(R.id.text_channel_subtitle)
		TextView subtitle;

		@BindView(R.id.image_channel_logo)
		ImageView logo;

		@BindView(R.id.program_info)
		ProgramInfoViewBase programInfoView;

		private ChannelModel channel;

		ViewHolder(View view) {
			super(view);
			ButterKnife.bind(this, view);

			view.setOnLongClickListener(this);
			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (channel != null) {
				ChannelListAdapter.this.listener.onItemClick(channel);
			}
		}

		@Override
		public boolean onLongClick(View view) {
			if (channel != null) {
				ChannelListAdapter.this.listener.onItemLongClick(channel, view);
			}
			return true;
		}

		void setChannel(ChannelModel channel) {
			this.channel = channel;

			logo.setImageResource(channel.getDrawableId());
			logo.setContentDescription(channel.getName());

			if (channel.getSubtitle() == null) {
				subtitle.setVisibility(View.GONE);
			} else {
				subtitle.setText(channel.getSubtitle());
				subtitle.setVisibility(View.VISIBLE);
			}

			programInfoView.setChannel(channel);
		}

		void pause() {
			programInfoView.pause();
		}

		void resume() {
			programInfoView.resume();
		}
	}

	public interface Listener {
		void onItemClick(ChannelModel channel);

		void onItemLongClick(ChannelModel channel, View view);
	}
}
