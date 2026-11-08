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
import kotlin.time.Duration.Companion.seconds

class ProgramInfoViewModel(
	application: Application,
	private val programInfoRepository: ProgramInfoRepository,
	private val emptyLiveShow: LiveShow,
) : AndroidViewModel(application) {

	private val channelId = MutableSharedFlow<String>(replay = 1)

	private val updateLiveShowTicker = flow {
		while (true) {
			emit(Unit)
			delay(UPDATE_PROGRAM_INFO_INTERVAL)
		}
	}

	private val updateShowProgressTicker = flow {
		while (true) {
			emit(Unit)
			delay(UPDATE_SHOW_PROGRESS_INTERVAL)
		}
	}

	private val _liveShow = updateLiveShowTicker
		.combine(channelId) { _, channelId -> programInfoRepository.getShow(channelId) }
		.catch { emit(emptyLiveShow) }
		.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
		.distinctUntilChanged()

	val liveShow = _liveShow
		.asLiveData(viewModelScope.coroutineContext)

	val titleFlow = _liveShow.map { it.title }
	val title = _liveShow
		.map { liveShow -> liveShow.title }
		.asLiveData(viewModelScope.coroutineContext)

	val subtitleFlow = _liveShow.map { it.subtitle }
	val subtitle = subtitleFlow.asLiveData(viewModelScope.coroutineContext)

	val descriptionFlow = _liveShow.map { it.description }
	val description = descriptionFlow.asLiveData(viewModelScope.coroutineContext)

	val timeFlow = _liveShow
		.map { liveShow ->
			if (liveShow.hasDuration) {
				val startTime = getTimeString(liveShow.startTime!!)
				val endTime = getTimeString(liveShow.endTime!!)
				application.getString(R.string.view_program_info_show_time, startTime, endTime)
			} else {
				null
			}
		}

	val progressPercentFlow = updateShowProgressTicker
		.combine(_liveShow) { _, liveShow ->
			if (liveShow.hasDuration) {
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
		private val UPDATE_PROGRAM_INFO_INTERVAL = 60.seconds
		private val UPDATE_SHOW_PROGRESS_INTERVAL = 1.seconds
	}
}
