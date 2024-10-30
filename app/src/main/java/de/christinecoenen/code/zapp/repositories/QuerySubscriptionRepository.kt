package de.christinecoenen.code.zapp.repositories

import de.christinecoenen.code.zapp.models.search.QuerySubscription
import de.christinecoenen.code.zapp.persistence.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuerySubscriptionRepository(private val database: Database) {
	suspend fun listSubscriptions() = withContext(Dispatchers.IO) {
		database
			.subscriptionDao()
			.getAll()
	}

	suspend fun saveSubscription(query: String) = withContext(Dispatchers.IO) {
		if (query.isBlank()) {
			return@withContext
		}

		database
			.subscriptionDao()
			.insert(QuerySubscription(query.trim()))
	}

	suspend fun isSubscribed(query: String) = withContext(Dispatchers.IO) {
		database
			.subscriptionDao()
			.exists(query)
	}

	suspend fun deleteSubscription(query: String) = withContext(Dispatchers.IO) {
		database
			.subscriptionDao()
			.delete(query)
	}

	suspend fun deleteAllSubscriptions() = withContext(Dispatchers.IO) {
		database
			.subscriptionDao()
			.deleteAll()
	}
}
