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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
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
		.combine(channelId) { _, channelId ->
			try {
				programInfoRepository.getShow(channelId)
			} catch (e: Exception) {
				LiveShow(application.getString(R.string.activity_channel_detail_info_error))
			}
		}
		.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
		.distinctUntilChanged()

	val titleFlow = liveShow.map { it.title }
	val title = titleFlow.asLiveData(viewModelScope.coroutineContext)

	val subtitleFlow = liveShow.map { it.subtitle }
	val subtitle = subtitleFlow.asLiveData(viewModelScope.coroutineContext)

	val descriptionFlow = liveShow.map { it.description }
	val description = descriptionFlow.asLiveData(viewModelScope.coroutineContext)

	val timeFlow = liveShow
		.map { liveShow ->
			if (liveShow.hasDuration()) {
				val startTime = getTimeString(liveShow.startTime!!)
				val endTime = getTimeString(liveShow.endTime!!)
				application.getString(R.string.view_program_info_show_time, startTime, endTime)
			} else {
				null
			}
		}

	val time = timeFlow.asLiveData(viewModelScope.coroutineContext)

	val progressPercentFlow = updateShowProgressTicker
		.combine(liveShow) { _, liveShow ->
			if (liveShow.hasDuration()) {
				liveShow.progressPercent
			} else {
				null
			}
		}

	val progressPercent = progressPercentFlow.asLiveData(viewModelScope.coroutineContext)

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
