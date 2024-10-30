package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.MediathekPagingSource
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.app.mediathek.api.result.QueryInfoResult
import de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter.UiModel
import de.christinecoenen.code.zapp.models.search.QuerySubscription
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.QuerySubscriptionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import org.joda.time.DateTime

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MediathekListFragmentViewModel(
	private val mediathekApi: IMediathekApiService,
	private val subscriptionRepository: QuerySubscriptionRepository
) : ViewModel() {
	val subscriptionsOnly: MutableStateFlow<Boolean> = MutableStateFlow(false)

	companion object {
		private const val ITEM_COUNT_PER_PAGE = 30
		private const val DEBOUNCE_TIME_MILLIS = 300L
	}

	private val pagingConfig = PagingConfig(
		pageSize = ITEM_COUNT_PER_PAGE,
		enablePlaceholders = false
	)

	private val _queryInfoResult = MutableStateFlow<QueryInfoResult?>(null)

	val pageFlow = subscriptionsOnly
		.mapLatest { subscriptionsOnly ->
			createQueryRequest(subscriptionsOnly)
		}
		.debounce(DEBOUNCE_TIME_MILLIS)
		.flatMapLatest { queryRequest ->
			Pager(pagingConfig) {
				MediathekPagingSource(mediathekApi, queryRequest, _queryInfoResult, subscriptionsOnly.value)
			}.flow
		}
		.map<PagingData<MediathekShow>, PagingData<UiModel>> { pagingData ->
			pagingData.map { show ->
				UiModel.MediathekShowModel(
					show,
					DateTime(show.timestamp.toLong() * DateUtils.SECOND_IN_MILLIS)
				)
			}
		}
		.cachedIn(viewModelScope)

	private suspend fun createQueryRequest(subscriptionsOnly: Boolean): QueryRequest {
		return QueryRequest().apply {
			size = ITEM_COUNT_PER_PAGE
			future = false

			if (subscriptionsOnly) {
				val subscriptions = subscriptionRepository
					.listSubscriptions()
					.map(QuerySubscription::query)
				setQueryStrings(subscriptions)
			}
		}
	}
}
