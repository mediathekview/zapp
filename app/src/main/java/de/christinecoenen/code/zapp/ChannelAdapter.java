package de.christinecoenen.code.zapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;

public class ChannelAdapter extends BaseAdapter {

	private Context context;
	private IChannelList channelList;

	public ChannelAdapter(Context context, IChannelList channelList) {
		this.context = context;
		this.channelList = channelList;
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
		ImageView imageView;
		if (convertView == null) {
			// if it's not recycled, initialize some attributes
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}

		ChannelModel channel = channelList.get(position);
		imageView.setImageResource(channel.getDrawableId());
		return imageView;
	}
}
