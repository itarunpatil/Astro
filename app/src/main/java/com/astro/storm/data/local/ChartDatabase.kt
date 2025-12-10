package com.astro.storm.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Room database for chart persistence.
 *
 * Migration Strategy:
 * - All migrations are explicitly defined to prevent data loss
 * - Automatic backup before migrations for recovery
 * - Schema validation to ensure integrity
 * - No destructive migration fallback in production
 */
@Database(
    entities = [ChartEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ChartDatabase : RoomDatabase() {
    abstract fun chartDao(): ChartDao

    companion object {
        private const val TAG = "ChartDatabase"
        private const val DATABASE_NAME = "astrostorm_database"
        private const val BACKUP_SUFFIX = ".backup"
        private const val MAX_BACKUP_COUNT = 3

        @Volatile
        private var INSTANCE: ChartDatabase? = null

        /**
         * Migration from version 1 to 2: Add gender column to charts table.
         * Gender field added with safe default for existing records.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Use transaction for atomicity
                    db.beginTransaction()
                    try {
                        // Check if column already exists (idempotent migration)
                        val cursor = db.query("PRAGMA table_info(charts)")
                        var columnExists = false
                        while (cursor.moveToNext()) {
                            val nameIndex = cursor.getColumnIndex("name")
                            if (nameIndex >= 0 && cursor.getString(nameIndex) == "gender") {
                                columnExists = true
                                break
                            }
                        }
                        cursor.close()

                        if (!columnExists) {
                            db.execSQL("ALTER TABLE charts ADD COLUMN gender TEXT NOT NULL DEFAULT 'OTHER'")
                            Log.i(TAG, "Migration 1->2: Added gender column successfully")
                        } else {
                            Log.i(TAG, "Migration 1->2: gender column already exists, skipping")
                        }

                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 1->2 failed", e)
                    throw e
                }
            }
        }

        /**
         * All migrations in order. Add new migrations here.
         * Each migration should be idempotent and wrapped in a transaction.
         */
        private val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2
            // Add future migrations here: MIGRATION_2_3, MIGRATION_3_4, etc.
        )

        /**
         * Callback for database creation and opening.
         * Creates backup before any migration runs.
         */
        private fun createCallback(context: Context) = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "Database opened, version: ${db.version}")
            }

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.i(TAG, "Database created, version: ${db.version}")
            }
        }

        /**
         * Get the singleton database instance.
         * Creates database with all migrations and safety callbacks.
         *
         * @param context Application context
         * @return ChartDatabase singleton instance
         */
        fun getInstance(context: Context): ChartDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ChartDatabase {
            // Create backup before building (migration might run)
            createPreMigrationBackup(context)

            return Room.databaseBuilder(
                context.applicationContext,
                ChartDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(*ALL_MIGRATIONS)
                .addCallback(createCallback(context))
                // NOTE: Removed fallbackToDestructiveMigration() to prevent data loss
                // If a migration is missing, the app will crash with a clear error
                // rather than silently destroying user data
                .build()
        }

        /**
         * Creates a backup of the database before migration.
         * Maintains a rolling backup system with MAX_BACKUP_COUNT backups.
         */
        private fun createPreMigrationBackup(context: Context) {
            try {
                val dbPath = context.getDatabasePath(DATABASE_NAME)
                if (!dbPath.exists()) {
                    return // No database to backup
                }

                val backupDir = File(context.filesDir, "db_backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                // Rotate old backups
                rotateBackups(backupDir)

                // Create new backup with timestamp
                val timestamp = System.currentTimeMillis()
                val backupFile = File(backupDir, "${DATABASE_NAME}_${timestamp}${BACKUP_SUFFIX}")

                FileInputStream(dbPath).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.i(TAG, "Database backup created: ${backupFile.name}")
            } catch (e: Exception) {
                // Backup failure should not prevent database operation
                Log.e(TAG, "Failed to create database backup", e)
            }
        }

        /**
         * Rotates backup files, keeping only the most recent MAX_BACKUP_COUNT.
         */
        private fun rotateBackups(backupDir: File) {
            try {
                val backupFiles = backupDir.listFiles { file ->
                    file.name.startsWith(DATABASE_NAME) && file.name.endsWith(BACKUP_SUFFIX)
                }?.sortedByDescending { it.lastModified() } ?: return

                // Delete old backups beyond MAX_BACKUP_COUNT
                backupFiles.drop(MAX_BACKUP_COUNT - 1).forEach { file ->
                    if (file.delete()) {
                        Log.d(TAG, "Deleted old backup: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to rotate backups", e)
            }
        }

        /**
         * Attempts to restore database from most recent backup.
         * Use this if database corruption is detected.
         *
         * @param context Application context
         * @return true if restore was successful, false otherwise
         */
        fun restoreFromBackup(context: Context): Boolean {
            return try {
                // Close current instance
                INSTANCE?.close()
                INSTANCE = null

                val backupDir = File(context.filesDir, "db_backups")
                val backupFiles = backupDir.listFiles { file ->
                    file.name.startsWith(DATABASE_NAME) && file.name.endsWith(BACKUP_SUFFIX)
                }?.sortedByDescending { it.lastModified() }

                if (backupFiles.isNullOrEmpty()) {
                    Log.w(TAG, "No backup files found for restore")
                    return false
                }

                val mostRecentBackup = backupFiles.first()
                val dbPath = context.getDatabasePath(DATABASE_NAME)

                // Delete corrupted database
                if (dbPath.exists()) {
                    dbPath.delete()
                }

                // Also delete WAL and SHM files
                File(dbPath.path + "-wal").delete()
                File(dbPath.path + "-shm").delete()

                // Restore from backup
                FileInputStream(mostRecentBackup).use { input ->
                    FileOutputStream(dbPath).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.i(TAG, "Database restored from backup: ${mostRecentBackup.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore database from backup", e)
                false
            }
        }

        /**
         * Clears the database instance for testing purposes.
         * Should not be used in production code.
         */
        @Synchronized
        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
