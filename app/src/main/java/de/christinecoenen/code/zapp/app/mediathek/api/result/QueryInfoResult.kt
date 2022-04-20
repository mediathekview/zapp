package de.christinecoenen.code.zapp.app.mediathek.api.result

data class QueryInfoResult(

	var filmlisteTimestamp: Long,
	var searchEngineTime: Float,
	var resultCount: Int,
	var totalResults: Int

)
