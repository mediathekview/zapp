package de.christinecoenen.code.zapp.app.mediathek.repository.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface MediathekShowDao {

	@Query("SELECT * FROM PersistedMediathekShow")
	fun getAll(): Flowable<List<PersistedMediathekShow>>

	@Query("SELECT * FROM PersistedMediathekShow WHERE apiId=:apiId")
	fun getFromApiId(apiId: String): Flowable<PersistedMediathekShow>

	@Insert
	fun insert(vararg show: PersistedMediathekShow) : Completable

	@Delete
	fun delete(show: PersistedMediathekShow) : Completable
}
