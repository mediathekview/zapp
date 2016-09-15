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

public class ChannelListAdapter extends BaseAdapter {

	private Context context;
	private IChannelList channelList;
	private LayoutInflater inflater;

	public ChannelListAdapter(Context context, IChannelList channelList) {
		this.context = context;
		this.channelList = channelList;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return channelList.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			// if it's not recycled, initialize
			convertView = inflater.inflate(R.layout.item_channel_list, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		ChannelModel channel = channelList.get(position);
		holder.logo.setImageResource(channel.getDrawableId());
		holder.name.setText(channel.getName());

		return convertView;
	}

	static class ViewHolder {
		@BindView(R.id.text_channel_name) TextView name;
		@BindView(R.id.image_channel_logo) ImageView logo;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
