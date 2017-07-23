package de.christinecoenen.code.zapp.app.settings.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;


public class ChannelSelectionAdapter extends DragItemAdapter<ChannelModel, ChannelSelectionAdapter.ViewHolder> {

	private final LayoutInflater inflater;

	public ChannelSelectionAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setHasStableIds(true);
	}

	@Override
	public ChannelSelectionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.item_channel_selection_list, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ChannelSelectionAdapter.ViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		ChannelModel channel = mItemList.get(position);
		holder.setChannel(channel);
	}

	@Override
	public long getItemId(int position) {
		return mItemList.get(position).getId().hashCode();
	}

	class ViewHolder extends DragItemAdapter.ViewHolder {

		@BindView(R.id.image_handle) ImageView handleView;
		@BindView(R.id.image_channel_logo) ImageView logoView;
		@BindView(R.id.text_channel_subtitle) TextView subtitle;

		ViewHolder(final View itemView) {
			super(itemView, R.id.image_handle, false);
			ButterKnife.bind(this, itemView);
		}

		void setChannel(ChannelModel channel) {
			logoView.setImageResource(channel.getDrawableId());
			handleView.setContentDescription(channel.getName());

			if (channel.getSubtitle() == null) {
				subtitle.setVisibility(View.GONE);
			} else {
				subtitle.setText(channel.getSubtitle());
				subtitle.setVisibility(View.VISIBLE);
			}
		}
	}
}
