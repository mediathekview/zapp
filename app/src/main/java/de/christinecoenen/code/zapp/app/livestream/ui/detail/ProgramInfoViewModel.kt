package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.app.livestream.repository.ChannelInfoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.joda.time.DateTime

class ProgramInfoViewModel(
	application: Application,
	private val channelInfoRepository: ChannelInfoRepository
) : AndroidViewModel(application) {

	private val channelId = MutableSharedFlow<String>(replay = 1)

	private val updateLiveShowTicker: Flow<Int> = (0..Int.MAX_VALUE)
		.asSequence()
		.asFlow()
		.onEach { tick ->
			if (tick > 0) {
				delay(1000L * 60)
			}
		}

	private val liveShow =
		updateLiveShowTicker.combine(channelId) { _, channelId ->
			channelInfoRepository.getShows(channelId)
		}.catch {
			emit(LiveShow(application.getString(R.string.activity_channel_detail_info_error)))
		}.distinctUntilChanged()

	val title = liveShow
		.map { liveShow -> liveShow.title }
		.asLiveData(viewModelScope.coroutineContext)

	val subtitle = liveShow
		.map { liveShow -> liveShow.subtitle }
		.asLiveData(viewModelScope.coroutineContext)

	val time = liveShow
		.map { liveShow ->
			if (liveShow.hasDuration()) {
				val startTime = getTimeString(liveShow.startTime!!)
				val endTime = getTimeString(liveShow.endTime!!)
				application.getString(R.string.view_program_info_show_time, startTime, endTime)
			} else {
				null
			}
		}
		.asLiveData(viewModelScope.coroutineContext)

	suspend fun setChannelId(channelId: String) {
		this.channelId.emit(channelId)
	}

	private fun getTimeString(time: DateTime): String {
		return DateUtils.formatDateTime(getApplication(), time.millis, DateUtils.FORMAT_SHOW_TIME)
	}
}
