package de.christinecoenen.code.zapp.app.settings.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.woxthebox.draglistview.DragItemAdapter;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.databinding.ActivityChannelSelectionItemBinding;
import de.christinecoenen.code.zapp.models.channels.ChannelModel;


class ChannelSelectionAdapter extends DragItemAdapter<ChannelModel, ChannelSelectionAdapter.ViewHolder> {

	private final LayoutInflater inflater;

	ChannelSelectionAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setHasStableIds(true);
	}

	@NonNull
	@Override
	public ChannelSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ActivityChannelSelectionItemBinding binding = ActivityChannelSelectionItemBinding.inflate(inflater, parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ChannelSelectionAdapter.ViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		ChannelModel channel = mItemList.get(position);
		holder.setChannel(channel);
	}

	@Override
	public long getUniqueItemId(int position) {
		return mItemList.get(position).getId().hashCode();
	}

	static class ViewHolder extends DragItemAdapter.ViewHolder {

		private ImageView handleView;
		private ImageView logoView;
		private TextView subtitle;

		private ChannelModel channel;

		ViewHolder(final ActivityChannelSelectionItemBinding binding) {
			super(binding.getRoot(), R.id.image_handle, false);

			handleView = binding.imageHandle;
			logoView = binding.logo;
			subtitle = binding.subtitle;
		}

		void setChannel(ChannelModel channel) {
			this.channel = channel;

			setVisibility();
			logoView.setImageResource(channel.getDrawableId());
			handleView.setContentDescription(channel.getName());

			if (channel.getSubtitle() == null) {
				subtitle.setVisibility(View.GONE);
			} else {
				subtitle.setText(channel.getSubtitle());
				subtitle.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onItemClicked(View view) {
			channel.toggleIsEnabled();
			setVisibility();
		}

		private void setVisibility() {
			float alpha = channel.isEnabled() ? 1 : 0.25f;
			logoView.setAlpha(alpha);
			subtitle.setAlpha(alpha);

			float handleAlpha = channel.isEnabled() ? 1 : 0.5f;
			handleView.setAlpha(handleAlpha);
		}
	}
}
