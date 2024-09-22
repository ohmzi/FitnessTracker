package com.ohmz.fitnessTracker.Data.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ohmz.fitnessTracker.Data.DAO.FitnessTrackerDao
import com.ohmz.fitnessTracker.Data.Entity.CardioSession
import com.ohmz.fitnessTracker.Data.Entity.PowerLiftingSession

@Database(
    entities = [CardioSession::class, PowerLiftingSession::class],
    version = 1,
    exportSchema = false
)
abstract class FitnessTrackerDatabase : RoomDatabase() {
    abstract fun fitnessTrackerDao(): FitnessTrackerDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessTrackerDatabase? = null

        fun getDatabase(context: Context): FitnessTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessTrackerDatabase::class.java,
                    "fitness_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
