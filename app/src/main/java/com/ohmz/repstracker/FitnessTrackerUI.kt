@file:OptIn(ExperimentalFoundationApi::class)

package com.ohmz.repstracker

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedCheckCircle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    size: Dp = 40.dp,
    label: String,
    onLabelChange: (String) -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isChecked) Color(0xFF4CAF50) else Color(0xFF37474F),
        label = "backgroundColor"
    )
    val cornerRadius = size / 4

    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(bounded = true, radius = size / 2)

    var isEditing by remember { mutableStateOf(false) }
    var editableLabel by remember { mutableStateOf(label) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val squareAnimation = rememberInfiniteTransition(label = "squareAnimation")
    val squareAlpha by squareAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "squareAlpha"
    )

    Box(
        modifier = Modifier
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
                onClick = {
                    if (!isEditing) {
                        onCheckedChange(!isChecked)
                        if (isChecked) {
                            isEditing = false
                            keyboardController?.hide()
                        }
                    }
                },
                onLongClick = {
                    if (!isChecked) {
                        isEditing = true
                        editableLabel = ""
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isChecked) {
            if (isEditing) {
                BasicTextField(
                    value = editableLabel,
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
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        isEditing = false
                        keyboardController?.hide()
                    }),
                    cursorBrush = SolidColor(Color.Red),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            innerTextField()
                        }
                    }
                )
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

@Composable
fun LabelProgressIndicator(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    val outlineColor by animateColorAsState(
        targetValue = if (progress >= 1f) Color(0xFF4CAF50) else Color.White,
        label = "outlineColorAnimation"
    )
    val phase = rememberInfiniteTransition(label = "phaseAnimation").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseValue"
    )

    Box(
        modifier = modifier
            .padding(start = 0.dp, top = 14.dp, bottom = 14.dp)
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                val cornerRadius = 16.dp.toPx()

                drawRoundRect(
                    color = outlineColor,
                    topLeft = Offset(-strokeWidth / 2, -strokeWidth / 2),
                    size = Size(size.width + strokeWidth, size.height + strokeWidth),
                    cornerRadius = CornerRadius(cornerRadius + strokeWidth / 2),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = if (progress >= 1f) {
                            PathEffect.dashPathEffect(
                                floatArrayOf(20f, 20f),
                                phase = phase.value * 40f
                            )
                        } else null
                    )
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF37474F))
            .drawWithContent {
                val cornerRadius = 16.dp.toPx()

                drawRoundRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset.Zero,
                    size = Size(size.width * animatedProgress, size.height),
                    cornerRadius = CornerRadius(cornerRadius),
                    style = Fill
                )

                drawContent()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(0.dp)
        )
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
        )
        Text(
            text = "Current Progress",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = Color.White
        )
    }
}

@Composable
fun WorkoutTypeSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Workout Type",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutTypeButton("Cardio", isSelected = false)
                WorkoutTypeButton("Power", isSelected = true)
            }
        }
    }
}

@Composable
fun WorkoutTypeButton(text: String, isSelected: Boolean) {
    Button(
        onClick = { /* Handle click */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Red else Color.LightGray
        ),
        shape = RoundedCornerShape(50),
        modifier = Modifier.width(100.dp)
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Black)
    }
}


@Preview
@Composable
fun FitnessTrackerUI() {
    var activities by remember {
        mutableStateOf(listOf("Shoulder Press", "Chest Press", "Lateral raises"))
    }
    val allSets = remember { listOf("Set 1", "Set 2", "Set 3", "Set 4", "Set 5", "Set 6") }

    var checkStates by remember { mutableStateOf(List(activities.size) { List(allSets.size) { false } }) }
    var visibleSetsCount by remember { mutableStateOf(allSets.size) }

    val overallProgress by remember(checkStates, visibleSetsCount) {
        derivedStateOf {
            val totalChecks = checkStates.sumOf { row -> row.take(visibleSetsCount).count { it } }
            val totalPossibleChecks = activities.size * visibleSetsCount
            if (totalPossibleChecks > 0) totalChecks.toFloat() / totalPossibleChecks else 0f
        }
    }

    Box(
        modifier = Modifier
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
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            TopBar()
            Spacer(modifier = Modifier.height(24.dp))
            ProgressCircle(progress = overallProgress)
            Spacer(modifier = Modifier.height(16.dp))
            WorkoutTypeSection()
            ActivityGrid(
                activities = activities,
                allSets = allSets,
                checkStates = checkStates,
                onCheckStateChange = { newCheckStates ->
                    checkStates = newCheckStates
                },
                onActivitiesChange = { newActivities ->
                    activities = newActivities
                    checkStates = List(activities.size) { rowIndex ->
                        checkStates.getOrNull(rowIndex) ?: List(allSets.size) { false }
                    }
                },
                onVisibleSetsCountChange = { newVisibleSetsCount ->
                    visibleSetsCount = newVisibleSetsCount
                }
            )
        }
    }
}

@Composable
fun ProgressCircle(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutLinearInEasing),
        label = "progressAnimation"
    )

    val color by animateColorAsState(
        targetValue = if (animatedProgress >= 1f) Color(0xFF4CAF50) else Color.Red,
        label = "colorAnimation"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (animatedProgress >= 1f) Color(0xFF4CAF50) else Color(0xFFFFA6A6),
        label = "backgroundColorAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnimation"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension - 8.dp.toPx()) / 2f
            val strokeWidth = 8.dp.toPx()
            val sweepAngle = animatedProgress * 360f

            // Draw background circle
            drawCircle(
                color = backgroundColor,
                radius = if (animatedProgress >= 1f) radius + 12f else radius,
                center = center,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = if (animatedProgress >= 1f) {
                        PathEffect.dashPathEffect(
                            floatArrayOf(20f, 20f),
                            phase = phase * 40f
                        )
                    } else null
                )
            )

            if (animatedProgress >= 1f) {
                drawCircle(
                    color = backgroundColor,
                    radius = radius,
                    center = center,
                    style = Fill
                )
            }

            // Draw progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Completed",
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}
@Composable
fun ActivityGrid(
    activities: List<String>,
    allSets: List<String>,
    checkStates: List<List<Boolean>>,
    onCheckStateChange: (List<List<Boolean>>) -> Unit,
    onActivitiesChange: (List<String>) -> Unit,
    onVisibleSetsCountChange: (Int) -> Unit
) {

    var zoomFactor by remember { mutableStateOf(1f) }
    val visibleSets by remember {
        derivedStateOf {
            val visibleCount = (allSets.size / zoomFactor).toInt().coerceIn(3, allSets.size)
            allSets.take(visibleCount)
        }
    }

    var labelStates by remember { mutableStateOf(List(activities.size) { List(allSets.size) { "50" } }) }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val rowHeight by remember { derivedStateOf { (80 * zoomFactor).coerceIn(80f, 160f).dp } }

    var newExerciseName by remember { mutableStateOf("") }

    val visibleSetsCount = (allSets.size / zoomFactor).toInt().coerceIn(3, allSets.size)
    onVisibleSetsCountChange(visibleSetsCount)

    Box(modifier = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, _, zoom, _ ->
            zoomFactor = (zoomFactor * zoom).coerceIn(1f, 1.6f)
            Log.d("zoomFactor", zoomFactor.toString())
        }
    }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderRow(visibleSets, rowHeight)

            ActivityList(
                activities = activities,
                visibleSets = visibleSets,
                checkStates = checkStates,
                labelStates = labelStates,
                rowHeight = rowHeight,
                onCheckStateChange = onCheckStateChange,
                onActivitiesChange = onActivitiesChange,
                onLabelChange = { newLabelStates -> labelStates = newLabelStates },
                lazyListState = lazyListState
            )

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
                        labelStates = labelStates.toMutableList().apply {
                            add(List(allSets.size) { "40" })
                        }
                        newExerciseName = ""
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(activities.size)
                        }
                    }
                }
            )
        }
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
                    .height(rowHeight / 2),
                contentAlignment = Alignment.Center
            ) {
                Text(day, color = Color.White, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ActivityList(
    activities: List<String>,
    visibleSets: List<String>,
    checkStates: List<List<Boolean>>,
    labelStates: List<List<String>>,
    rowHeight: Dp,
    onCheckStateChange: (List<List<Boolean>>) -> Unit,
    onActivitiesChange: (List<String>) -> Unit,
    onLabelChange: (List<List<String>>) -> Unit,
    lazyListState: LazyListState
) {
    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(items = activities, key = { _, item -> item }) { index, activity ->
            ActivityRow(
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
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val checkedCount = checkStates.getOrNull(index)?.count { it } ?: 0
    val totalCount = visibleSets.size
    val progress = remember(checkedCount, totalCount) {
        if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f
    }

    Box(
        Modifier
            .padding(vertical = 4.dp)
            .offset { IntOffset(animatedOffset.roundToInt(), 0) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .draggable(
                    orientation = Orientation.Horizontal,
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
                    }
                )
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
                    AnimatedCheckCircle(
                        isChecked = checkStates.getOrNull(index)?.getOrNull(colIndex) ?: false,
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNewExerciseRow(
    newExerciseName: String,
    onNewExerciseNameChange: (String) -> Unit,
    onAddExercise: () -> Unit
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