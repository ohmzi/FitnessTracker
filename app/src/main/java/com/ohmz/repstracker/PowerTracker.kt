package com.ohmz.repstracker

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun PowerTracker(
    activities: List<String>,
    allSets: List<String>,
    checkStates: List<List<Boolean>>,
    onCheckStateChange: (List<List<Boolean>>) -> Unit,
    onActivitiesChange: (List<String>) -> Unit,
    onVisibleSetsCountChange: (Int) -> Unit,
    isExpanded: Boolean
) {
    var zoomFactor by remember { mutableStateOf(1f) }
    val visibleSets by remember {
        derivedStateOf {
            val visibleCount = (allSets.size / zoomFactor).toInt().coerceIn(3, allSets.size)
            allSets.take(visibleCount)
        }
    }

    var labelStates by remember { mutableStateOf(List(activities.size) { List(allSets.size) { "50" } }) }

    val rowHeight by remember { derivedStateOf { (80 * zoomFactor).coerceIn(80f, 160f).dp } }

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
        HeaderRow(visibleSets, rowHeight)

        activities.forEachIndexed { index, activity ->
            ActivityRow(activities = activities,
                activity = activity,
                index = index,
                visibleSets = visibleSets,
                checkStates = checkStates,
                labelStates = labelStates,
                rowHeight = rowHeight,
                onCheckStateChange = onCheckStateChange,
                onActivitiesChange = onActivitiesChange,
                onLabelChange = { newLabelStates -> labelStates = newLabelStates })
        }

        AddNewExerciseRow(newExerciseName = newExerciseName,
            onNewExerciseNameChange = { newExerciseName = it },
            onAddExercise = {
                if (newExerciseName.isNotBlank()) {
                    val newActivities = activities + newExerciseName
                    onActivitiesChange(newActivities)
                    val newCheckStates = checkStates.toMutableList().apply {
                        add(List(allSets.size) { false })
                    }
                    onCheckStateChange(newCheckStates)
                    labelStates = labelStates.toMutableList().apply {
                        add(List(allSets.size) { "40" })
                    }
                    newExerciseName = ""
                }
            })
        Spacer(modifier = Modifier.height(50.dp))

    }
}

@Composable
private fun HeaderRow(visibleSets: List<String>, rowHeight: Dp) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 30.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .height(rowHeight / 2),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Type", color = Color.White)
        }
        visibleSets.forEach { day ->
            Box(
                Modifier
                    .weight(1f)
                    .height(rowHeight / 2), contentAlignment = Alignment.Center
            ) {
                Text(day, color = Color.White, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ActivityRow(
    activities: List<String>,
    activity: String,
    index: Int,
    visibleSets: List<String>,
    checkStates: List<List<Boolean>>,
    labelStates: List<List<String>>,
    rowHeight: Dp,
    onCheckStateChange: (List<List<Boolean>>) -> Unit,
    onActivitiesChange: (List<String>) -> Unit,
    onLabelChange: (List<List<String>>) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val dismissThreshold = -200f

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val checkedCount = checkStates.getOrNull(index)?.count { it } ?: 0
    val totalCount = visibleSets.size
    val progress = remember(checkedCount, totalCount) {
        if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f
    }

    Box(
        Modifier
            .padding(vertical = 4.dp)
            .offset { IntOffset(animatedOffset.roundToInt(), 0) }) {
        Row(
            Modifier
                .fillMaxWidth()
                .draggable(orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX += delta
                        offsetX = offsetX.coerceAtMost(0f)
                    },
                    onDragStopped = {
                        if (offsetX < dismissThreshold) {
                            // Remove the activity
                            onActivitiesChange(activities.filterIndexed { i, _ -> i != index })
                            onCheckStateChange(checkStates.filterIndexed { i, _ -> i != index })
                            onLabelChange(labelStates.filterIndexed { i, _ -> i != index })
                        } else {
                            offsetX = 0f
                        }
                    })
        ) {
            LabelProgressIndicator(
                label = activity,
                progress = progress,
                modifier = Modifier
                    .weight(1.5f)
                    .height(rowHeight)
                    .padding(start = 16.dp, end = 8.dp)
            )
            visibleSets.forEachIndexed { colIndex, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedCheckCircle(isChecked = checkStates.getOrNull(index)
                        ?.getOrNull(colIndex) ?: false,
                        onCheckedChange = { newState ->
                            val newCheckStates = checkStates.mapIndexed { rowIdx, row ->
                                if (rowIdx == index) {
                                    row.mapIndexed { colIdx, col ->
                                        if (colIdx == colIndex) newState else col
                                    }
                                } else row
                            }
                            onCheckStateChange(newCheckStates)
                        },
                        size = (rowHeight.value * 0.6).dp,
                        label = labelStates.getOrNull(index)?.getOrNull(colIndex) ?: "50",
                        onLabelChange = { newLabel ->
                            val newLabelStates = labelStates.mapIndexed { rowIdx, row ->
                                if (rowIdx == index) {
                                    val baseValue = newLabel.toIntOrNull() ?: 40
                                    row.mapIndexed { colIdx, currentValue ->
                                        when {
                                            colIdx < colIndex -> currentValue
                                            colIdx == colIndex -> newLabel
                                            else -> (baseValue + (colIdx - colIndex) * 10).toString()
                                        }
                                    }
                                } else row
                            }
                            onLabelChange(newLabelStates)
                        })
                }
            }
        }
    }
}

@Composable
private fun AddNewExerciseRow(
    newExerciseName: String, onNewExerciseNameChange: (String) -> Unit, onAddExercise: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = newExerciseName,
            onValueChange = onNewExerciseNameChange,
            label = { Text("New Exercise", color = Color.White) },
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Button(
            onClick = onAddExercise,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Add", color = Color.Black)
        }
    }
}
