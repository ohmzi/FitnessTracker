package com.ohmz.repstracker

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.remember

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityGrid() {
    val activities = remember { mutableStateListOf("Exercise", "Read", "Meditate", "Study", "Cook", "Work", "Relax") }
    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    var zoomFactor by remember { mutableStateOf(1f) }
    val visibleDays by remember { derivedStateOf { allDays.take((allDays.size / zoomFactor).toInt().coerceAtLeast(1)) } }

    var checkStates by remember { mutableStateOf(List(activities.size) { List(allDays.size) { false } }) }

    val state = rememberTransformableState { zoomChange, _, _ ->
        zoomFactor *= zoomChange
        zoomFactor = zoomFactor.coerceIn(1f, allDays.size.toFloat())
    }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val rowHeight by remember { derivedStateOf { (60 * zoomFactor).coerceIn(60.0F, 180.0F).dp } }

    val visibleRowCount by remember {
        derivedStateOf {
            val visibleHeight = lazyListState.layoutInfo.viewportEndOffset - lazyListState.layoutInfo.viewportStartOffset
            (visibleHeight / rowHeight.value).toInt().coerceAtMost(activities.size)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .transformable(state = state)
    ) {
        Column {
            // Header row
            Row(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(rowHeight)
                        .border(1.dp, Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Activity", textAlign = TextAlign.Center)
                }
                visibleDays.forEach { day ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(rowHeight)
                            .border(1.dp, Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day, textAlign = TextAlign.Center)
                    }
                }
            }

            // Activity rows
            LazyColumn(state = lazyListState) {
                itemsIndexed(
                    items = activities,
                    key = { index, item -> item } // Use the activity as the key
                ) { index, activity ->
                    var offsetX by remember { mutableStateOf(0f) }
                    val dismissThreshold = -200f // Negative value for right-to-left swipe

                    val animatedOffset by animateFloatAsState(
                        targetValue = offsetX,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
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
                                .draggable(
                                    orientation = Orientation.Horizontal,
                                    state = rememberDraggableState { delta ->
                                        offsetX += delta
                                        offsetX = offsetX.coerceAtMost(0f) // Prevent dragging to the right
                                    },
                                    onDragStopped = {
                                        if (offsetX < dismissThreshold) {
                                            // Delete the item
                                            coroutineScope.launch {
                                                activities.removeAt(index)
                                                checkStates = checkStates.toMutableList().apply { removeAt(index) }
                                            }
                                        } else {
                                            // Reset the offset
                                            offsetX = 0f
                                        }
                                    }
                                )
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(rowHeight)
                                    .border(1.dp, Color.Gray),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(activity, modifier = Modifier.padding(start = 8.dp))
                            }
                            visibleDays.forEachIndexed { colIndex, _ ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(rowHeight)
                                        .border(1.dp, Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedCheckCircle2(
                                        isChecked = checkStates.getOrNull(index)?.getOrNull(colIndex) ?: false,
                                        onCheckedChange = { newState ->
                                            checkStates = checkStates.mapIndexed { rowIdx, row ->
                                                if (rowIdx == index) {
                                                    row.mapIndexed { colIdx, col ->
                                                        if (colIdx == colIndex) newState else col
                                                    }
                                                } else row
                                            }
                                        },
                                        size = (rowHeight.value * 0.6).dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Display visible row count
        Text(
            text = "Visible Rows: $visibleRowCount",
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
    label: String = "40KG"
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isChecked) Color.Green else Color.Gray,
        animationSpec = tween(durationMillis = 300)
    )

    val ringAnimation = rememberInfiniteTransition(label = "")
    val ringAlpha by ringAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = size / 2),
                onClick = { onCheckedChange(!isChecked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = this.center
            val radius = size.toPx() / 2

            // Main circle
            drawCircle(
                color = animatedColor,
                radius = radius
            )

            // Animated ring
            if (isChecked) {
                drawCircle(
                    color = Color.Green.copy(alpha = 1f - ringAlpha),
                    radius = radius + (size * 0.075f).toPx() * ringAlpha,
                    style = Stroke(width = (size * 0.05f).toPx())
                )
            }
        }

        // Label or Checkmark
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
                contentDescription = "Checkmark",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}