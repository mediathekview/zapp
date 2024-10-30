package de.christinecoenen.code.zapp.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.christinecoenen.code.zapp.models.search.QuerySubscription

@Dao
interface QuerySubscriptionDao {
	@Query("SELECT * FROM QuerySubscription")
	fun getAll(): List<QuerySubscription>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(querySubscription: QuerySubscription)

	@Query("DELETE FROM QuerySubscription WHERE `query` = :query")
	fun delete(query: String)

	@Query("DELETE FROM QuerySubscription")
	fun deleteAll()

	@Query("SELECT EXISTS(SELECT * FROM QuerySubscription WHERE `query` = :query)")
	suspend fun exists(query: String): Boolean
}
