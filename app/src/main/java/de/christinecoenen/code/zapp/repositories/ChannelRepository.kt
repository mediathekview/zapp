package de.christinecoenen.code.zapp.repositories

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.christinecoenen.code.zapp.app.livestream.api.IZappBackendApiService
import de.christinecoenen.code.zapp.app.livestream.api.model.ChannelInfo
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableVisibleJsonChannelList
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import de.christinecoenen.code.zapp.utils.io.IoUtils.writeAllText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

@SuppressLint("CheckResult")
class ChannelRepository(
	private val context: Context,
	scope: CoroutineScope,
	private val zappApi: IZappBackendApiService
) {

	companion object {

		private const val CHANNEL_INFOS_FILE_NAME = "channelInfoList.json"

	}

	private val gson = Gson()
	private val channelList: ISortableChannelList
	private var channelInfoList: Map<String, ChannelInfo> = emptyMap()

	fun getChannelList(): ISortableChannelList {
		channelList.reload()
		applyToChannelList(channelInfoList)

		return channelList
	}

	fun deleteCachedChannelInfos() {
		deleteChannelInfoListFromDisk()
		channelList.reload()
	}

	private fun onChannelInfoListSuccess(channelInfoList: Map<String, ChannelInfo>) {
		try {
			writeChannelInfoListToDisk(channelInfoList)
		} catch (e: IOException) {
			Timber.e(e)
		}

		this.channelInfoList = channelInfoList
		applyToChannelList(channelInfoList)
	}

	private fun applyToChannelList(channelInfoList: Map<String, ChannelInfo>) {
		for (mapEntry in channelInfoList.entries) {
			val channel = channelList[mapEntry.key]

			if (channel != null) {
				channel.streamUrl = mapEntry.value.streamUrl
			}
		}
	}

	private suspend fun getChannelInfoListFromApi(): Map<String, ChannelInfo> {
		return zappApi.getChannelInfoList()
	}

	@Throws(IOException::class)
	private fun getChannelInfoListFromDisk(): Map<String, ChannelInfo> {
		context.openFileInput(CHANNEL_INFOS_FILE_NAME).use { inputStream ->
			val json = inputStream.readAllText()
			val type = object : TypeToken<Map<String?, ChannelInfo?>?>() {}.type
			return gson.fromJson(json, type)
		}
	}

	@Throws(IOException::class)
	private fun writeChannelInfoListToDisk(channelInfoList: Map<String, ChannelInfo>) {
		context.openFileOutput(CHANNEL_INFOS_FILE_NAME, Context.MODE_PRIVATE)
			.use { fileOutputStream ->
				val json = gson.toJson(channelInfoList)
				fileOutputStream.writeAllText(json)
			}
	}

	private fun deleteChannelInfoListFromDisk() {
		context.deleteFile(CHANNEL_INFOS_FILE_NAME)
	}

	init {
		channelList = SortableVisibleJsonChannelList(context)

		scope.launch(Dispatchers.IO) {

			try {
				// load fresh urls from api
				getChannelInfoListFromApi()
			} catch (e: Exception) {
				// if api does not work, load cached list
				try {
					getChannelInfoListFromDisk()
				} catch (e: Exception) {
					// refresh failed - use bundled channel list
					null
				}
			}?.let {
				onChannelInfoListSuccess(it)
			}
		}
	}
}
