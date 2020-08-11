package de.christinecoenen.code.zapp.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow

@Database(entities = [PersistedMediathekShow::class], version = 1, exportSchema = true)
@TypeConverters(DownloadStatusConverter::class, DateTimeConverter::class)
abstract class Database : RoomDatabase() {

	companion object {

		fun getInstance(applicationContext: Context): de.christinecoenen.code.zapp.persistence.Database {
			return Room
				.databaseBuilder(applicationContext, de.christinecoenen.code.zapp.persistence.Database::class.java, "zapp.db")
				.build()
		}

	}

	abstract fun mediathekShowDao(): MediathekShowDao

}
