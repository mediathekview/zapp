package de.christinecoenen.code.zapp.app.mediathek.api.result

import androidx.annotation.Keep

@Keep
data class QueryInfoResult(

	var filmlisteTimestamp: Long,
	var searchEngineTime: Float,
	var resultCount: Int,
	var totalResults: Int

)
