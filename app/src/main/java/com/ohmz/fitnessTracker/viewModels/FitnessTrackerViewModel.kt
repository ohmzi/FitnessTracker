package com.ohmz.fitnessTracker.viewModels

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ohmz.fitnessTracker.services.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PowerWorkoutState(
    val activities: List<String> = listOf(
        "Shoulder Press", "Curls", "Chest Press", "Lateral raises", "Leg raises"
    ),
    val allSets: List<String> = listOf("Set 1", "Set 2", "Set 3", "Set 4", "Set 5", "Set 6"),
    val checkStates: List<List<Boolean>> = List(5) { List(6) { false } },
    val labelStates: List<List<String>> = List(5) { List(6) { "50" } },
    val visibleSetsCount: Int = 6
)

@Serializable
data class CardioWorkoutState(
    val distance: Float = 0f,
    val targetDistance: Float = 5f,
    val time: Long = 0L,
    val pace: Float = 0f
)

@Serializable
data class FitnessTrackerState(
    val powerWorkout: PowerWorkoutState = PowerWorkoutState(),
    val cardioWorkout: CardioWorkoutState = CardioWorkoutState(),
    val isPowerExpanded: Boolean = false,
    val isCardioExpanded: Boolean = false
)

class FitnessTrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FitnessTrackerState())
    val uiState: StateFlow<FitnessTrackerState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    private var startTime: Long = 0
    private var isTracking = false


    init {
        loadState()
        startTimeUpdates()
    }

    fun updatePowerWorkoutCheckState(newCheckStates: List<List<Boolean>>) {
        updateState { currentState ->
            currentState.copy(
                powerWorkout = currentState.powerWorkout.copy(
                    checkStates = newCheckStates
                )
            )
        }
    }

    fun updatePowerWorkoutActivities(newActivities: List<String>) {
        updateState { currentState ->
            currentState.copy(
                powerWorkout = currentState.powerWorkout.copy(
                    activities = newActivities,
                    checkStates = List(newActivities.size) { rowIndex ->
                        currentState.powerWorkout.checkStates.getOrNull(rowIndex)
                            ?: List(currentState.powerWorkout.allSets.size) { false }
                    },
                    labelStates = List(newActivities.size) { rowIndex ->
                        currentState.powerWorkout.labelStates.getOrNull(rowIndex)
                            ?: List(currentState.powerWorkout.allSets.size) { "50" }
                    }
                )
            )
        }
    }

    fun updatePowerWorkoutVisibleSetsCount(newCount: Int) {
        updateState { currentState ->
            currentState.copy(
                powerWorkout = currentState.powerWorkout.copy(
                    visibleSetsCount = newCount
                )
            )
        }
    }

    fun updatePowerWorkoutLabelStates(newLabelStates: List<List<String>>) {
        updateState { currentState ->
            currentState.copy(
                powerWorkout = currentState.powerWorkout.copy(
                    labelStates = newLabelStates
                )
            )
        }
    }

    fun updateCardioWorkoutDistance(newDistance: Float) {
        updateState { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    distance = newDistance
                )
            )
        }
    }

    fun updateCardioWorkoutTargetDistance(newTargetDistance: Float) {
        updateState { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    targetDistance = newTargetDistance
                )
            )
        }
    }

    private fun startTimeUpdates() {
        viewModelScope.launch {
            while (true) {
                if (isTracking) {
                    updateCardioWorkoutTime(System.currentTimeMillis() - startTime)
                }
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    fun startCardioWorkout() {
        startTime = System.currentTimeMillis() - _uiState.value.cardioWorkout.time
        isTracking = true
    }

    fun stopCardioWorkout() {
        isTracking = false
    }

    fun updateCardioWorkoutTime(newTime: Long) {
        updateState { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    time = newTime
                )
            )
        }
    }

    fun updateCardioWorkoutPace(newPace: Float) {
        updateState { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    pace = newPace
                )
            )
        }
    }

    fun togglePowerExpanded() {
        updateState { currentState ->
            currentState.copy(
                isPowerExpanded = !currentState.isPowerExpanded,
                isCardioExpanded = false
            )
        }
    }

    fun toggleCardioExpanded() {
        updateState { currentState ->
            currentState.copy(
                isCardioExpanded = !currentState.isCardioExpanded,
                isPowerExpanded = false
            )
        }
    }

    fun resetCardioWorkout() {
        updateState { currentState ->
            currentState.copy(
                cardioWorkout = CardioWorkoutState(targetDistance = currentState.cardioWorkout.targetDistance)
            )
        }
    }

    private fun updateState(update: (FitnessTrackerState) -> FitnessTrackerState) {
        _uiState.value = update(_uiState.value)
        viewModelScope.launch {
            saveState()
        }
    }


    private fun saveState() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "FitnessTrackerPrefs",
            Context.MODE_PRIVATE
        )
        with(sharedPreferences.edit()) {
            putString("uiState", Json.encodeToString(_uiState.value))
            apply()
        }
    }

    private fun loadState() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "FitnessTrackerPrefs",
            Context.MODE_PRIVATE
        )
        val savedState = sharedPreferences.getString("uiState", null)
        if (savedState != null) {
            try {
                _uiState.value = Json.decodeFromString(savedState)
            } catch (e: Exception) {
                _uiState.value = FitnessTrackerState()
            }
        }
    }

    fun startNotificationService() {
        val intent = Intent(getApplication(), NotificationService::class.java)
        getApplication<Application>().startForegroundService(intent)
    }

    fun stopNotificationService() {
        val intent = Intent(getApplication(), NotificationService::class.java)
        getApplication<Application>().stopService(intent)
    }

    fun getNotificationText(): String {
        val state = uiState.value
        return when {
            state.isCardioExpanded -> {
                val distance = state.cardioWorkout.distance / 1000f // Convert to km
                val time = formatTime(state.cardioWorkout.time)
                "Distance: ${"%.2f".format(distance)} km, Time: $time"
            }

            state.isPowerExpanded -> {
                val completedSets = state.powerWorkout.checkStates.sumOf { row -> row.count { it } }
                val totalSets =
                    state.powerWorkout.activities.size * state.powerWorkout.visibleSetsCount
                "Completed sets: $completedSets/$totalSets"
            }

            else -> "Fitness Tracker Running"
        }
    }

    private fun formatTime(timeInMillis: Long): String {
        val seconds = (timeInMillis / 1000) % 60
        val minutes = (timeInMillis / (1000 * 60)) % 60
        val hours = (timeInMillis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    companion object {
        @Volatile
        private var instance: FitnessTrackerViewModel? = null

        fun getInstance(application: Application): FitnessTrackerViewModel {
            return instance ?: synchronized(this) {
                instance ?: FitnessTrackerViewModel(application).also { instance = it }
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            if (modelClass.isAssignableFrom(FitnessTrackerViewModel::class.java)) {
                return getInstance(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}