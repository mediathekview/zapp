package de.christinecoenen.code.zapp.repositories;

import android.content.Context;

import java.util.Map;

import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo;
import de.christinecoenen.code.zapp.app.livestream.repository.ChannelInfoRepository;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.ISortableChannelList;
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class ChannelRepository {

	private final ISortableChannelList channelList;

	public ChannelRepository(Context context) {
		channelList = new SortableJsonChannelList(context);

		// TODO: on error load from disk
		Disposable disposable = getChannelInfoList()
			.subscribe(this::onChannelInfoListSuccess, Timber::w);
	}

	public ISortableChannelList getChannelList() {
		return channelList;
	}

	public void deleteCachedChannelInfos() {
		// TODO: delete channelInfoList from disk
		// TODO: reload channelList
	}

	private void onChannelInfoListSuccess(Map<String, ChannelInfo> channelInfoList) {
		// TODO: persist to disk
		applyToChannelList(channelInfoList);
	}

	private void applyToChannelList(Map<String, ChannelInfo> channelInfoList) {
		for (String channelId : channelInfoList.keySet()) {
			ChannelModel channel = channelList.get(channelId);
			if (channel != null) {
				channel.setStreamUrl(channelInfoList.get(channelId).getStreamUrl());
			}
		}
	}

	private Single<Map<String, ChannelInfo>> getChannelInfoList() {
		return ChannelInfoRepository.getInstance()
			.getChannelInfoList();
	}
}
