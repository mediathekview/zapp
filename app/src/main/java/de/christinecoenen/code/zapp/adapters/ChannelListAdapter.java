package de.christinecoenen.code.zapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.WeakHashMap;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.databinding.ItemChannelListBinding;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder> {

	@SuppressWarnings("unused")
	private static final String TAG = ChannelListAdapter.class.getSimpleName();

	private final IChannelList channelList;
	private final LayoutInflater inflater;
	private final WeakHashMap<ViewHolder, ?> visibleViews = new WeakHashMap<>();
	private final Listener listener;

	public ChannelListAdapter(Context context, IChannelList channelList, Listener listener) {
		this.channelList = channelList;
		this.listener = listener;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setHasStableIds(true);
	}

	public void pause() {
		for (ViewHolder holder : visibleViews.keySet()) {
			holder.pause();
		}
	}

	public void resume() {
		notifyDataSetChanged();
		for (ViewHolder holder : visibleViews.keySet()) {
			holder.resume();
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ItemChannelListBinding binding =
			DataBindingUtil.inflate(inflater, R.layout.item_channel_list, parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		ChannelModel channel = channelList.get(position);
		holder.setChannel(channel);
	}

	@Override
	public void onViewDetachedFromWindow(ViewHolder holder) {
		holder.pause();
		visibleViews.remove(holder);
	}

	@Override
	public void onViewAttachedToWindow(ViewHolder holder) {
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

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		private final ItemChannelListBinding binding;
		private ChannelModel channel;

		ViewHolder(ItemChannelListBinding binding) {
			super(binding.getRoot());

			this.binding = binding;

			binding.getRoot().setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (channel != null) {
				ChannelListAdapter.this.listener.onItemClick(channel);
			}
		}

		void setChannel(ChannelModel channel) {
			this.channel = channel;

			binding.setChannel(channel);
			binding.programInfo.setChannel(channel);
		}

		void pause() {
			binding.programInfo.pause();
		}

		void resume() {
			binding.programInfo.resume();
		}
	}

	public interface Listener {
		void onItemClick(ChannelModel channel);
	}
}
