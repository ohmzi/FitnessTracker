package com.ohmz.fitnessTracker.UI.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ohmz.fitnessTracker.Data.Entity.CardioSession
import com.ohmz.fitnessTracker.Data.Entity.PowerLiftingSession
import com.ohmz.fitnessTracker.Data.Repository.FitnessTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FitnessTrackerViewModel(private val repository: FitnessTrackerRepository) : ViewModel() {
    private val _cardioSessions = MutableStateFlow<List<CardioSession>>(emptyList())
    val cardioSessions: StateFlow<List<CardioSession>> = _cardioSessions.asStateFlow()

    private val _powerLiftingSessions = MutableStateFlow<List<PowerLiftingSession>>(emptyList())
    val powerLiftingSessions: StateFlow<List<PowerLiftingSession>> =
        _powerLiftingSessions.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allCardioSessions.collect { sessions ->
                _cardioSessions.value = sessions
            }
        }
        viewModelScope.launch {
            repository.allPowerLiftingSessions.collect { sessions ->
                _powerLiftingSessions.value = sessions
            }
        }
    }

    fun saveCardioSession(distance: Float, duration: Long) {
        viewModelScope.launch {
            val cardioSession = CardioSession(distance = distance, duration = duration)
            repository.insertCardioSession(cardioSession)
        }
    }

    fun savePowerLiftingSession(exercise: String, weight: Int, reps: Int, sets: Int) {
        viewModelScope.launch {
            val powerLiftingSession =
                PowerLiftingSession(exercise = exercise, weight = weight, reps = reps, sets = sets)
            repository.insertPowerLiftingSession(powerLiftingSession)
        }
    }
}

class FitnessTrackerViewModelFactory(private val repository: FitnessTrackerRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessTrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
