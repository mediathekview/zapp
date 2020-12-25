package de.christinecoenen.code.zapp.app.livestream.ui.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.WeakHashMap;

import de.christinecoenen.code.zapp.app.livestream.ui.views.ProgramInfoViewBase;
import de.christinecoenen.code.zapp.databinding.FragmentChannelListItemBinding;
import de.christinecoenen.code.zapp.models.channels.ChannelModel;
import de.christinecoenen.code.zapp.models.channels.IChannelList;

class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder> {

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
		FragmentChannelListItemBinding binding = FragmentChannelListItemBinding.inflate(inflater, parent, false);
		return new ViewHolder(binding);
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

		private TextView subtitle;
		private ImageView logo;
		private ProgramInfoViewBase programInfoView;

		private ChannelModel channel;

		ViewHolder(FragmentChannelListItemBinding binding) {
			super(binding.getRoot());

			binding.getRoot().setOnLongClickListener(this);
			binding.getRoot().setOnClickListener(this);

			subtitle = binding.textChannelSubtitle;
			logo = binding.imageChannelLogo;
			programInfoView = binding.programInfo;
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
