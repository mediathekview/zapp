package de.christinecoenen.code.zapp.app.livestream.ui

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.app.livestream.repository.ProgramInfoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.joda.time.DateTime

class ProgramInfoViewModel(
	application: Application,
	private val programInfoRepository: ProgramInfoRepository
) : AndroidViewModel(application) {

	private val channelId = MutableSharedFlow<String>(replay = 1)

	private val updateLiveShowTicker = flow {
		while (true) {
			emit(Unit)
			delay(1000L * UPDATE_PROGRAM_INFO_INTERVAL_SECONDS)
		}
	}

	private val updateShowProgressTicker = flow {
		while (true) {
			emit(Unit)
			delay(1000L * UPDATE_SHOW_PROGRESS_INTERVAL_SECONDS)
		}
	}

	private val liveShow = updateLiveShowTicker
		.combine(channelId) { _, channelId -> programInfoRepository.getShows(channelId) }
		.catch { emit(LiveShow(application.getString(R.string.activity_channel_detail_info_error))) }
		.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
		.distinctUntilChanged()

	val title = liveShow
		.map { liveShow -> liveShow.title }
		.asLiveData(viewModelScope.coroutineContext)

	val subtitle = liveShow
		.map { liveShow -> liveShow.subtitle }
		.asLiveData(viewModelScope.coroutineContext)

	val description = liveShow
		.map { liveShow -> liveShow.description }
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

	val progressPercent = updateShowProgressTicker
		.combine(liveShow) { _, liveShow ->
			if (liveShow.hasDuration()) {
				liveShow.progressPercent
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

	companion object {
		private const val UPDATE_PROGRAM_INFO_INTERVAL_SECONDS = 60
		private const val UPDATE_SHOW_PROGRESS_INTERVAL_SECONDS = 1
	}
}
