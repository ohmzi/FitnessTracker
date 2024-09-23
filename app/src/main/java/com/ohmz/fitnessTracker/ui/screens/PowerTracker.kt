package com.ohmz.fitnessTracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ohmz.fitnessTracker.ui.components.AddNewExerciseRow
import com.ohmz.fitnessTracker.ui.components.WorkoutListGrid
import com.ohmz.fitnessTracker.ui.components.WorkoutListGridColumnLabel

@Composable
fun PowerTracker(
    activities: List<String>,
    allSets: List<String>,
    checkStates: List<List<Boolean>>,
    onCheckStateChange: (List<List<Boolean>>) -> Unit,
    onActivitiesChange: (List<String>) -> Unit,
    onVisibleSetsCountChange: (Int) -> Unit,
    isExpanded: Boolean,
    labelStates: List<List<String>>,
    onLabelChange: (List<List<String>>) -> Unit
) {
    var zoomFactor by remember { mutableStateOf(1f) }
    val visibleSets = remember(zoomFactor) {
        val visibleCount = (allSets.size / zoomFactor).toInt().coerceIn(3, allSets.size)
        allSets.take(visibleCount)
    }

    val rowHeight by remember { mutableStateOf((80 * zoomFactor).coerceIn(80f, 160f).dp) }

    var newExerciseName by remember { mutableStateOf("") }

    val visibleSetsCount = (allSets.size / zoomFactor).toInt().coerceIn(3, allSets.size)
    onVisibleSetsCountChange(visibleSetsCount)

    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    Column(modifier = Modifier
        .fillMaxWidth()
        .alpha(alpha)
        .pointerInput(Unit) {
            detectTransformGestures { _, _, zoom, _ ->
                zoomFactor = (zoomFactor * zoom).coerceIn(1f, 1.6f)
            }
        }) {
        WorkoutListGridColumnLabel(visibleSets, rowHeight)

        activities.forEachIndexed { index, activity ->
            WorkoutListGrid(
                activities = activities,
                activity = activity,
                index = index,
                visibleSets = visibleSets,
                checkStates = checkStates,
                labelStates = labelStates,
                rowHeight = rowHeight,
                onCheckStateChange = onCheckStateChange,
                onActivitiesChange = onActivitiesChange,
                onLabelChange = onLabelChange
            )
        }

        AddNewExerciseRow(
            newExerciseName = newExerciseName,
            onNewExerciseNameChange = { newExerciseName = it },
            onAddExercise = {
                if (newExerciseName.isNotBlank()) {
                    val newActivities = activities + newExerciseName
                    onActivitiesChange(newActivities)
                    val newCheckStates = checkStates.toMutableList().apply {
                        add(List(allSets.size) { false })
                    }
                    onCheckStateChange(newCheckStates)
                    val newLabelStates = labelStates.toMutableList().apply {
                        add(List(allSets.size) { "40" })
                    }
                    onLabelChange(newLabelStates)
                    newExerciseName = ""
                }
            }
        )
        Spacer(modifier = Modifier.height(50.dp))
    }
}