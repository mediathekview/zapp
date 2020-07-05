package de.christinecoenen.code.zapp.app.mediathek.repository.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow

@Database(entities = [PersistedMediathekShow::class], version = 1, exportSchema = true)
@TypeConverters(DownloadStatusConverter::class, DateTimeConverter::class)
abstract class MediathekDatabase : RoomDatabase() {

	companion object {

		fun getInstance(applicationContext: Context): MediathekDatabase {
			return Room
				.databaseBuilder(applicationContext, MediathekDatabase::class.java, "mediathek.db")
				.build()
		}

	}

	abstract fun mediathekShowDao(): MediathekShowDao

}
