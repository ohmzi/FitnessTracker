package com.ohmz.fitnessTracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedCheckCircle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    size: Dp = 40.dp,
    label: String,
    onLabelChange: (String) -> Unit
) {
    var showEditPopup by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        if (isChecked) Color(0xFF4CAF50) else Color(0xFF37474F), label = "backgroundColor"
    )
    val cornerRadius = size / 4

    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(bounded = true, radius = size / 2)

    val squareAnimation = rememberInfiniteTransition(label = "squareAnimation")
    val squareAlpha by squareAnimation.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1000), repeatMode = RepeatMode.Reverse
        ), label = "squareAlpha"
    )

    Box(modifier = Modifier
        .size(size)
        .drawBehind {
            if (isChecked) {
                val animatedSize = size.toPx() + 10.dp.toPx() * squareAlpha
                val offset = (animatedSize - size.toPx()) / 2

                drawRoundRect(
                    color = Color(0xFF4CAF50).copy(alpha = 1f - squareAlpha),
                    topLeft = Offset(-offset, -offset),
                    size = Size(animatedSize, animatedSize),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        .background(backgroundColor, RoundedCornerShape(cornerRadius))
        .indication(interactionSource, indication)
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onCheckedChange(!isChecked) },
            onLongClick = { if (!isChecked) showEditPopup = true }
        ), contentAlignment = Alignment.Center
    ) {
        if (!isChecked) {
            Text(
                text = label,
                color = Color.White,
                fontSize = (size.value * 0.3).sp,
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }

    if (showEditPopup) {
        LabelEditPopup(
            currentValue = label.toIntOrNull() ?: 0,
            onValueChange = { newValue ->
                onLabelChange(newValue.toString())
                showEditPopup = false
            },
            onDismiss = { showEditPopup = false }
        )
    }
}

@Composable
fun LabelEditPopup(
    currentValue: Int,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedValue by remember { mutableStateOf(currentValue) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Value", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                NumberPicker(
                    value = selectedValue,
                    onValueChange = { selectedValue = it },
                    range = (0..100),
                    step = 5
                )

                Spacer(modifier = Modifier.height(16.dp))
                AnimatedButton2(
                    onClick = { onValueChange(selectedValue) },
                    lightColor = Color(0xFF90EE90),
                    darkColor = Color(0xFF32CD32),
                    modifier = Modifier.fillMaxWidth(),
                    content = { Text("Confirm", color = Color.Black) }
                )
            }
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedButton2(
            onClick = { onValueChange((value - step).coerceIn(range)) },
            lightColor = Color(0xFFFFCCCB),
            darkColor = Color(0xFFFF6961),
            content = { Text("-", color = Color.Black) }
        )
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        AnimatedButton2(
            onClick = { onValueChange((value + step).coerceIn(range)) },
            lightColor = Color(0xFFADD8E6),
            darkColor = Color(0xFF6495ED),
            content = { Text("+", color = Color.Black) }
        )
    }
}

@Composable
fun AnimatedButton2(
    onClick: () -> Unit,
    lightColor: Color,
    darkColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) darkColor else lightColor,
        animationSpec = tween(durationMillis = 100)
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer(
            scaleX = scale,
            scaleY = scale
        )
    ) {
        content()
    }
}