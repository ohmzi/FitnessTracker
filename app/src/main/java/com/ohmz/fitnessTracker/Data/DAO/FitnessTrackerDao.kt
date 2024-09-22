package com.ohmz.fitnessTracker.Data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ohmz.fitnessTracker.Data.Entity.CardioSession
import com.ohmz.fitnessTracker.Data.Entity.PowerLiftingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessTrackerDao {
    @Insert
    suspend fun insertCardioSession(session: CardioSession)

    @Insert
    suspend fun insertPowerLiftingSession(session: PowerLiftingSession)

    @Query("SELECT * FROM cardio_sessions ORDER BY date DESC")
    fun getAllCardioSessions(): Flow<List<CardioSession>>

    @Query("SELECT * FROM power_lifting_sessions ORDER BY date DESC")
    fun getAllPowerLiftingSessions(): Flow<List<PowerLiftingSession>>
}