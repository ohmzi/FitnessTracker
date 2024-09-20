package com.ohmz.fitnessTracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
fun WorkoutListGrid(
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
            IndividualWorkoutButton(
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
                    WeightButtons(
                        isChecked = checkStates.getOrNull(index)
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
                                    val baseValue = newLabel.toIntOrNull() ?: return@mapIndexed row
                                    row.mapIndexed { colIdx, _ ->
                                        when {
                                            colIdx < colIndex -> row[colIdx]
                                            colIdx == colIndex -> newLabel
                                            else -> (baseValue + (colIdx - colIndex) * 10).coerceIn(
                                                0,
                                                999
                                            ).toString()
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
fun WorkoutListGridColumnLabel(visibleSets: List<String>, rowHeight: Dp) {
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
