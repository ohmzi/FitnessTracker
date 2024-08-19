@file:OptIn(ExperimentalFoundationApi::class)

package com.ohmz.repstracker


import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityGrid() {
    val activities =
        remember { mutableStateListOf("Shoulder Press", "Chest Press", "Lateral raises") }
    val allDays = listOf("Set 1", "Set 2", "Set 3", "Set 4", "Set 5", "Set 6")

    var zoomFactor by remember { mutableFloatStateOf(1f) }
    val visibleDays by remember {
        derivedStateOf {
            allDays.take(
                (allDays.size / zoomFactor).toInt().coerceAtLeast(1)
            )
        }
    }

    var checkStates by remember { mutableStateOf(List(activities.size) { List(allDays.size) { false } }) }
    var labelStates by remember { mutableStateOf(List(activities.size) { List(allDays.size) { "40" } }) }

    val state = rememberTransformableState { zoomChange, _, _ ->
        zoomFactor *= zoomChange
        zoomFactor = zoomFactor.coerceIn(1f, allDays.size.toFloat())
    }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val rowHeight by remember { derivedStateOf { (80 * zoomFactor).coerceIn(80.0F, 200.0F).dp } }

    val visibleCheckboxCount by remember {
        derivedStateOf {
            val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
            val firstVisibleRow = visibleItemsInfo.firstOrNull()?.index ?: 0
            val lastVisibleRow = visibleItemsInfo.lastOrNull()?.index ?: 0
            (lastVisibleRow - firstVisibleRow + 1) * visibleDays.size
        }
    }

    var newExerciseName by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF69B4),  // Pink
                        Color(0xFFFF8C00),  // Dark Orange
                        Color(0xFF4169E1)   // Royal Blue
                    )
                )
            )
            .transformable(state = state),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        ) {
            // Header row
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    Modifier
                        .weight(1.5f)
                        .height(rowHeight),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "Exercise",
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                visibleDays.forEach { day ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(rowHeight), contentAlignment = Alignment.Center
                    ) {
                        Text(day, color = Color.White, textAlign = TextAlign.Center)
                    }
                }
            }

            // Activity rows
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(items = activities, key = { index, item -> item }) { index, activity ->
                    var offsetX by remember { mutableFloatStateOf(0f) }
                    val dismissThreshold = -200f

                    val animatedOffset by animateFloatAsState(
                        targetValue = offsetX, animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ), label = ""
                    )

                    Box(
                        Modifier.animateItemPlacement(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        // Delete button (hidden off-screen to the right)
                        Box(
                            Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = rowHeight)
                                .size(rowHeight)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }

                        // Main row content
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                                .draggable(orientation = Orientation.Horizontal,
                                    state = rememberDraggableState { delta ->
                                        offsetX += delta
                                        offsetX = offsetX.coerceAtMost(0f)
                                    },
                                    onDragStopped = {
                                        if (offsetX < dismissThreshold) {
                                            coroutineScope.launch {
                                                activities.removeAt(index)
                                                checkStates = checkStates
                                                    .toMutableList()
                                                    .apply { removeAt(index) }
                                                labelStates = labelStates
                                                    .toMutableList()
                                                    .apply { removeAt(index) }
                                            }
                                        } else {
                                            offsetX = 0f
                                        }
                                    })
                        ) {
                            Box(
                                Modifier
                                    .weight(1.5f)
                                    .height(rowHeight),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    activity,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 16.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            visibleDays.forEachIndexed { colIndex, _ ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(rowHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val labelValue =
                                        labelStates.getOrNull(index)?.getOrNull(colIndex) ?: "40"
                                    AnimatedCheckCircle2(isChecked = checkStates.getOrNull(index)
                                        ?.getOrNull(colIndex) ?: false,
                                        onCheckedChange = { newState ->
                                            checkStates = checkStates.mapIndexed { rowIdx, row ->
                                                if (rowIdx == index) {
                                                    row.mapIndexed { colIdx, col ->
                                                        if (colIdx == colIndex) newState else col
                                                    }
                                                } else row
                                            }
                                        },
                                        size = (rowHeight.value * 0.6).dp,
                                        label = labelValue,
                                        onLabelChange = { newLabel ->
                                            labelStates = labelStates.mapIndexed { rowIdx, row ->
                                                if (rowIdx == index) {
                                                    row.mapIndexed { colIdx, col ->
                                                        if (colIdx == colIndex) {
                                                            newLabel
                                                        } else {
                                                            val prevValue = col.toIntOrNull() ?: 40
                                                            val newValue =
                                                                if (colIdx == colIndex + 1) {
                                                                    (newLabel.toIntOrNull()
                                                                        ?: prevValue) + 10
                                                                } else {
                                                                    prevValue
                                                                }
                                                            newValue.toString()
                                                        }
                                                    }
                                                } else row
                                            }
                                        })
                                }
                            }
                        }
                    }
                    if (index == activities.lastIndex) {
                        // Add new exercise row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(value = newExerciseName,
                                onValueChange = { newExerciseName = it },
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
                                onClick = {
                                    if (newExerciseName.isNotBlank()) {
                                        activities.add(newExerciseName)
                                        checkStates = checkStates.toMutableList().apply {
                                            add(List(allDays.size) { false })
                                        }
                                        labelStates = labelStates.toMutableList().apply {
                                            add(List(allDays.size) { "40" })
                                        }
                                        newExerciseName = ""
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(activities.size - 1)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text("Add", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Display visible checkbox count
        Text(
            text = "Visible Checkboxes: $visibleCheckboxCount",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}


@Composable
fun AnimatedCheckCircle2(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    size: Dp = 40.dp,
    label: String,
    onLabelChange: (String) -> Unit
) {
    val backgroundColor = if (isChecked) Color(0xFF4CAF50) else Color(0xFF37474F)
    val cornerRadius = size / 4

    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(bounded = true, radius = size / 2)

    var isEditing by remember { mutableStateOf(false) }
    var editableLabel by remember { mutableStateOf(label) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .indication(interactionSource, indication)
            .combinedClickable(interactionSource = interactionSource, indication = null, onClick = {
                if (!isEditing) {
                    onCheckedChange(!isChecked)
                    if (isChecked) {
                        isEditing = false
                        keyboardController?.hide()
                    }
                }
            }, onLongClick = {
                if (!isChecked) {
                    isEditing = true
                    editableLabel = ""
                }
            }), contentAlignment = Alignment.Center
    ) {
        if (!isChecked) {
            if (isEditing) {
                BasicTextField(value = editableLabel,
                    onValueChange = {
                        editableLabel = it
                        onLabelChange(it)
                    },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = (size.value * 0.3).sp,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxSize(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        isEditing = false
                        keyboardController?.hide()
                    }),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                        ) {
                            innerTextField()
                        }
                    })
            } else {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = (size.value * 0.3).sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
