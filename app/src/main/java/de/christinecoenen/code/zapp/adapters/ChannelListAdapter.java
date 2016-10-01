package de.christinecoenen.code.zapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.views.ProgramInfoViewBase;

public class ChannelListAdapter extends BaseAdapter {

	private final IChannelList channelList;
	private final LayoutInflater inflater;

	public ChannelListAdapter(Context context, IChannelList channelList) {
		this.channelList = channelList;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return channelList.size();
	}

	public Object getItem(int position) {
		return channelList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			// if it's not recycled, initialize
			convertView = inflater.inflate(R.layout.item_channel_list, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		ChannelModel channel = channelList.get(position);
		holder.setChannel(channel);

		return convertView;
	}

	static class ViewHolder {
		@BindView(R.id.text_channel_subtitle) TextView subtitle;
		@BindView(R.id.image_channel_logo) ImageView logo;
		@BindView(R.id.program_info) ProgramInfoViewBase programInfoView;

		ViewHolder(View view) {
			ButterKnife.bind(this, view);
			programInfoView.resume();
		}

		void setChannel(ChannelModel channel) {
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
	}
}
