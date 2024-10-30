package de.christinecoenen.code.zapp.persistence

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.christinecoenen.code.zapp.models.search.QuerySubscription
import de.christinecoenen.code.zapp.models.search.SearchQuery
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow

@Database(
	entities = [PersistedMediathekShow::class, SearchQuery::class, QuerySubscription::class],
	version = 5,
	autoMigrations = [
		AutoMigration(from = 3, to = 4),
		AutoMigration(from = 4, to = 5)
	],
	exportSchema = true
)
@TypeConverters(DownloadStatusConverter::class, DateTimeConverter::class)
abstract class Database : RoomDatabase() {

	companion object {

		/**
		 * Add bookmark feature
		 */
		private val MIGRATION_2_3 = object : Migration(2, 3) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE PersistedMediathekShow ADD COLUMN bookmarked INTEGER NOT NULL DEFAULT 0")
				db.execSQL("ALTER TABLE PersistedMediathekShow ADD COLUMN bookmarkedAt INTEGER")
				db.execSQL("ALTER TABLE PersistedMediathekShow ADD COLUMN showUpdatedAt INTEGER")
				db.execSQL("UPDATE PersistedMediathekShow SET showUpdatedAt=createdAt")
			}
		}

		/**
		 * Migration to kotlin where some columns are now no longer nullable.
		 */
		private val MIGRATION_1_2 = object : Migration(1, 2) {
			override fun migrate(db: SupportSQLiteDatabase) {
				// delete shows without api ids (should not be there anyway)
				db.execSQL("DELETE FROM PersistedMediathekShow WHERE apiId IS NULL OR trim(apiId)='';")

				// delete shows without channels (should not be there anyway)
				db.execSQL("DELETE FROM PersistedMediathekShow WHERE channel IS NULL OR trim(channel)='';")

				// delete shows without default url (should not be there anyway)
				db.execSQL("DELETE FROM PersistedMediathekShow WHERE videoUrl IS NULL OR trim(videoUrl)='';")

				// set null values to empty string
				db.execSQL("UPDATE PersistedMediathekShow set topic='' where topic IS NULL;")
				db.execSQL("UPDATE PersistedMediathekShow set title='' where title IS NULL;")

				// recreate table with new column data types (not nullable)
				db.execSQL("ALTER TABLE PersistedMediathekShow RENAME TO PersistedMediathekShow_old;")
				db.execSQL("CREATE TABLE IF NOT EXISTS PersistedMediathekShow (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'createdAt' INTEGER NOT NULL, 'downloadId' INTEGER NOT NULL, 'downloadedAt' INTEGER, 'downloadedVideoPath' TEXT, 'downloadStatus' INTEGER NOT NULL, 'downloadProgress' INTEGER NOT NULL, 'lastPlayedBackAt' INTEGER, 'playbackPosition' INTEGER NOT NULL, 'videoDuration' INTEGER NOT NULL, 'apiId' TEXT NOT NULL, 'topic' TEXT NOT NULL, 'title' TEXT NOT NULL, 'description' TEXT, 'channel' TEXT NOT NULL, 'timestamp' INTEGER NOT NULL, 'size' INTEGER NOT NULL, 'duration' TEXT, 'filmlisteTimestamp' INTEGER NOT NULL, 'websiteUrl' TEXT, 'subtitleUrl' TEXT, 'videoUrl' TEXT NOT NULL, 'videoUrlLow' TEXT, 'videoUrlHd' TEXT);")
				db.execSQL("INSERT INTO PersistedMediathekShow SELECT * FROM PersistedMediathekShow_old;")
				db.execSQL("DROP TABLE PersistedMediathekShow_old;")
				db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_PersistedMediathekShow_apiId ON PersistedMediathekShow (apiId)")
			}
		}


		fun getInstance(applicationContext: Context): de.christinecoenen.code.zapp.persistence.Database {
			return Room
				.databaseBuilder(
					applicationContext,
					de.christinecoenen.code.zapp.persistence.Database::class.java,
					"zapp.db"
				)
				.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
				.build()
		}

	}

	abstract fun mediathekShowDao(): MediathekShowDao

	abstract fun searchDao(): SearchDao

	abstract fun subscriptionDao(): QuerySubscriptionDao
}
