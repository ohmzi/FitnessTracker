package com.ohmz.fitnessTracker.Data.Repository

import com.ohmz.fitnessTracker.Data.DAO.FitnessTrackerDao
import com.ohmz.fitnessTracker.Data.Entity.CardioSession
import com.ohmz.fitnessTracker.Data.Entity.PowerLiftingSession
import kotlinx.coroutines.flow.Flow

class FitnessTrackerRepository(private val fitnessTrackerDao: FitnessTrackerDao) {
    val allCardioSessions: Flow<List<CardioSession>> = fitnessTrackerDao.getAllCardioSessions()
    val allPowerLiftingSessions: Flow<List<PowerLiftingSession>> =
        fitnessTrackerDao.getAllPowerLiftingSessions()

    suspend fun insertCardioSession(session: CardioSession) {
        fitnessTrackerDao.insertCardioSession(session)
    }

    suspend fun insertPowerLiftingSession(session: PowerLiftingSession) {
        fitnessTrackerDao.insertPowerLiftingSession(session)
    }
}
