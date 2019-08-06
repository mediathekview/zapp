package de.christinecoenen.code.zapp.repositories;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
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

	private final String CHANNEL_INFOS_FILE_NAME = "channelInfoList.json";

	private final Gson gson = new Gson();
	private final Context context;
	private final ISortableChannelList channelList;

	public ChannelRepository(Context context) {
		this.context = context;

		channelList = new SortableJsonChannelList(context);

		Disposable disposable = getChannelInfoListFromApi()
			.onErrorReturn(t -> getChannelInfoListFromDisk())
			.subscribe(this::onChannelInfoListSuccess, Timber::w);
	}

	public ISortableChannelList getChannelList() {
		return channelList;
	}

	public void deleteCachedChannelInfos() {
		deleteChannelInfoListFromDisk();
		channelList.reload();
	}

	private void onChannelInfoListSuccess(Map<String, ChannelInfo> channelInfoList) {
		try {
			writeChannelInfoListToDisk(channelInfoList);
		} catch (IOException e) {
			Timber.e(e);
		}

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

	private Single<Map<String, ChannelInfo>> getChannelInfoListFromApi() {
		return ChannelInfoRepository.getInstance()
			.getChannelInfoList();
	}

	private Map<String, ChannelInfo> getChannelInfoListFromDisk() throws IOException {
		try (FileInputStream inputStream = context.openFileInput(CHANNEL_INFOS_FILE_NAME)) {
			String json = IOUtils.toString(inputStream, "UTF-8");
			inputStream.close();
			Type type = new TypeToken<Map<String, ChannelInfo>>() {
			}.getType();
			return gson.fromJson(json, type);
		}
	}

	private void writeChannelInfoListToDisk(Map<String, ChannelInfo> channelInfoList) throws IOException {
		try (FileOutputStream fileOutputStream = context.openFileOutput(CHANNEL_INFOS_FILE_NAME, Context.MODE_PRIVATE)) {
			String json = gson.toJson(channelInfoList);
			IOUtils.write(json, fileOutputStream, "UTF-8");
		}
	}

	private void deleteChannelInfoListFromDisk() {
		context.deleteFile(CHANNEL_INFOS_FILE_NAME);
	}
}
