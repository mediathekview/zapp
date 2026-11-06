package de.christinecoenen.code.zapp.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import de.christinecoenen.code.zapp.models.collections.ShowCollection
import de.christinecoenen.code.zapp.models.collections.ShowCollectionEntry
import de.christinecoenen.code.zapp.models.search.SearchQuery
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow

@Database(
	entities = [
		PersistedMediathekShow::class,
		SearchQuery::class,
		ShowCollection::class,
		ShowCollectionEntry::class
	],
	version = 13,
	autoMigrations = [],
	exportSchema = true
)
@TypeConverters(DownloadStatusConverter::class, DateTimeConverter::class)
abstract class Database : RoomDatabase() {

	companion object {

		/**
		 * Channels that are searched for a collection.
		 */
		private val MIGRATION_12_13 = object : Migration(12, 13) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN channels TEXT NOT NULL DEFAULT ''")
			}
		}

		/**
		 * Sort direction of the episode sorting of a collection.
		 */
		private val MIGRATION_11_12 = object : Migration(11, 12) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN sortEpisodesAscending INTEGER NOT NULL DEFAULT 1")
			}
		}

		/**
		 * Episode detection and sorting for collections plus optional season/episode numbers
		 * delivered by the mediathek.
		 */
		private val MIGRATION_10_11 = object : Migration(10, 11) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN detectEpisodes INTEGER NOT NULL DEFAULT 0")
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN sortByEpisodes INTEGER NOT NULL DEFAULT 0")
				db.execSQL("ALTER TABLE PersistedMediathekShow ADD COLUMN season INTEGER")
				db.execSQL("ALTER TABLE PersistedMediathekShow ADD COLUMN episode INTEGER")
			}
		}

		/**
		 * Store which shows belong to a collection, so that the counters can be calculated
		 * exactly. The cached counters are invalidated so they are rebuilt for all collections.
		 */
		private val MIGRATION_9_10 = object : Migration(9, 10) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("CREATE TABLE IF NOT EXISTS `ShowCollectionEntry` (`collectionId` INTEGER NOT NULL, `apiId` TEXT NOT NULL, PRIMARY KEY(`collectionId`, `apiId`))")
				db.execSQL("UPDATE ShowCollection SET countUpdatedAt = NULL")
			}
		}

		/**
		 * Invalidate the cached collection counters. Excluded shows are subtracted from the
		 * total since the exclusion feature exists, so old values have to be recalculated.
		 */
		private val MIGRATION_8_9 = object : Migration(8, 9) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("UPDATE ShowCollection SET countUpdatedAt = NULL")
			}
		}

		/**
		 * Remember the newest known show of a collection to be able to detect new shows
		 */
		private val MIGRATION_7_8 = object : Migration(7, 8) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN lastKnownShowTimestamp INTEGER NOT NULL DEFAULT 0")
			}
		}

		/**
		 * Replace the "mark all as watched" flag of collections with excluded search terms
		 */
		private val MIGRATION_6_7 = object : Migration(6, 7) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE ShowCollection RENAME TO ShowCollection_old")
				db.execSQL("CREATE TABLE IF NOT EXISTS `ShowCollection` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `searchQuery` TEXT NOT NULL, `excludeTerms` TEXT NOT NULL, `totalCount` INTEGER NOT NULL, `unwatchedCount` INTEGER NOT NULL, `countUpdatedAt` INTEGER, `createdAt` INTEGER NOT NULL)")
				db.execSQL("INSERT INTO ShowCollection (id, name, searchQuery, excludeTerms, totalCount, unwatchedCount, countUpdatedAt, createdAt) SELECT id, name, searchQuery, '', totalCount, unwatchedCount, countUpdatedAt, createdAt FROM ShowCollection_old")
				db.execSQL("DROP TABLE ShowCollection_old")
			}
		}

		/**
		 * Add counters for show collections
		 */
		private val MIGRATION_5_6 = object : Migration(5, 6) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN totalCount INTEGER NOT NULL DEFAULT 0")
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN unwatchedCount INTEGER NOT NULL DEFAULT 0")
				db.execSQL("ALTER TABLE ShowCollection ADD COLUMN countUpdatedAt INTEGER DEFAULT NULL")
			}
		}

		/**
		 * Add series collections feature
		 */
		private val MIGRATION_4_5 = object : Migration(4, 5) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL("CREATE TABLE IF NOT EXISTS `ShowCollection` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `searchQuery` TEXT NOT NULL, `markAllAsWatched` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
			}
		}

		private val MIGRATION_3_4 = object : Migration(3, 4) {
			override fun migrate(db: SupportSQLiteDatabase) {
				try {
					db.execSQL("ALTER TABLE `PersistedMediathekShow` ADD COLUMN `showUpdatedAt` INTEGER DEFAULT NULL")
				} catch (_: SQLiteException) {
					// this is okay - the column is already present when updating from 9.0.0-beta to newer versions
				}

				db.execSQL("CREATE TABLE IF NOT EXISTS `SearchQuery` (`query` TEXT NOT NULL, `date` INTEGER NOT NULL, PRIMARY KEY(`query`))")
			}
		}

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
				.addMigrations(
					MIGRATION_1_2,
					MIGRATION_2_3,
					MIGRATION_3_4,
					MIGRATION_4_5,
					MIGRATION_5_6,
					MIGRATION_6_7,
					MIGRATION_7_8,
					MIGRATION_8_9,
					MIGRATION_9_10,
					MIGRATION_10_11,
					MIGRATION_11_12,
					MIGRATION_12_13
				)
				.build()
		}

	}

	abstract fun mediathekShowDao(): MediathekShowDao

	abstract fun searchDao(): SearchDao

	abstract fun showCollectionDao(): ShowCollectionDao

	abstract fun showCollectionEntryDao(): ShowCollectionEntryDao

}
