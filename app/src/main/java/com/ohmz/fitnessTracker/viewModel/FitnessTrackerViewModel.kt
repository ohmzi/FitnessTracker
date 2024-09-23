package com.ohmz.fitnessTracker.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PowerWorkoutState(
    val activities: List<String> = listOf(
        "Shoulder Press", "Curls", "Chest Press", "Lateral raises", "Leg raises"
    ),
    val allSets: List<String> = listOf("Set 1", "Set 2", "Set 3", "Set 4", "Set 5", "Set 6"),
    val checkStates: List<List<Boolean>> = List(5) { List(6) { false } },
    val labelStates: List<List<String>> = List(5) { List(6) { "50" } },
    val visibleSetsCount: Int = 6
)

data class CardioWorkoutState(
    val distance: Float = 0f,
    val targetDistance: Float = 5f,
    val time: Long = 0L,
    val pace: Float = 0f
)

data class FitnessTrackerState(
    val powerWorkout: PowerWorkoutState = PowerWorkoutState(),
    val cardioWorkout: CardioWorkoutState = CardioWorkoutState(),
    val isPowerExpanded: Boolean = false,
    val isCardioExpanded: Boolean = false
)

class FitnessTrackerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FitnessTrackerState())
    val uiState: StateFlow<FitnessTrackerState> = _uiState.asStateFlow()

    fun updatePowerWorkoutCheckState(newCheckStates: List<List<Boolean>>) {
        _uiState.update { currentState ->
            currentState.copy(
                powerWorkout = currentState.powerWorkout.copy(
                    checkStates = newCheckStates
                )
            )
        }
    }

    fun updatePowerWorkoutActivities(newActivities: List<String>) {
        _uiState.update { currentState ->
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
        _uiState.update { currentState ->
            currentState.copy(
                powerWorkout = currentState.powerWorkout.copy(
                    visibleSetsCount = newCount
                )
            )
        }
    }

    fun updatePowerWorkoutLabelStates(newLabelStates: List<List<String>>) {
        _uiState.update { currentState ->
            currentState.copy(
                powerWorkout = currentState.powerWorkout.copy(
                    labelStates = newLabelStates
                )
            )
        }
    }

    fun updateCardioWorkoutDistance(newDistance: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    distance = newDistance
                )
            )
        }
    }

    fun updateCardioWorkoutTargetDistance(newTargetDistance: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    targetDistance = newTargetDistance
                )
            )
        }
    }

    fun updateCardioWorkoutTime(newTime: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    time = newTime
                )
            )
        }
    }

    fun updateCardioWorkoutPace(newPace: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                cardioWorkout = currentState.cardioWorkout.copy(
                    pace = newPace
                )
            )
        }
    }

    fun togglePowerExpanded() {
        _uiState.update { currentState ->
            currentState.copy(
                isPowerExpanded = !currentState.isPowerExpanded,
                isCardioExpanded = false
            )
        }
    }

    fun toggleCardioExpanded() {
        _uiState.update { currentState ->
            currentState.copy(
                isCardioExpanded = !currentState.isCardioExpanded,
                isPowerExpanded = false
            )
        }
    }

    fun resetCardioWorkout() {
        _uiState.update { currentState ->
            currentState.copy(
                cardioWorkout = CardioWorkoutState(targetDistance = currentState.cardioWorkout.targetDistance)
            )
        }
    }
}