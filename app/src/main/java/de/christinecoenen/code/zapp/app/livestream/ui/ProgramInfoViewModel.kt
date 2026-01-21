package de.christinecoenen.code.zapp.app.livestream.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.app.livestream.repository.ProgramInfoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class ProgramInfoViewModel(
	private val programInfoRepository: ProgramInfoRepository,
	private val emptyLiveShow: LiveShow,
) : ViewModel() {

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

	private val _liveShow = updateLiveShowTicker
		.combine(channelId) { _, channelId -> programInfoRepository.getShow(channelId) }
		.catch { emit(emptyLiveShow) }
		.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
		.distinctUntilChanged()

	val liveShow = _liveShow
		.asLiveData(viewModelScope.coroutineContext)

	val title = _liveShow
		.map { liveShow -> liveShow.title }
		.asLiveData(viewModelScope.coroutineContext)

	val progressPercent = updateShowProgressTicker
		.combine(_liveShow) { _, liveShow ->
			if (liveShow.hasDuration) {
				liveShow.progressPercent
			} else {
				null
			}
		}
		.asLiveData(viewModelScope.coroutineContext)

	suspend fun setChannelId(channelId: String) {
		this.channelId.emit(channelId)
	}

	companion object {
		private const val UPDATE_PROGRAM_INFO_INTERVAL_SECONDS = 60
		private const val UPDATE_SHOW_PROGRESS_INTERVAL_SECONDS = 1
	}
}
