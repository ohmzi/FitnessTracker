@file:OptIn(ExperimentalFoundationApi::class)

package com.ohmz.repstracker

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
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
        if (isChecked) Color(0xFF4CAF50) else Color(0xFF37474F), label = "backgroundColor"
    )
    val cornerRadius = size / 4

    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(bounded = true, radius = size / 2)

    var isEditing by remember { mutableStateOf(false) }
    var editableLabel by remember { mutableStateOf(label) }

    val keyboardController = LocalSoftwareKeyboardController.current

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
                    cursorBrush = SolidColor(Color.Red),
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

@Composable
fun LabelProgressIndicator(
    label: String, progress: Float, modifier: Modifier = Modifier
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
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "phaseValue"
    )

    Box(modifier = modifier
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
                    width = strokeWidth, pathEffect = if (progress >= 1f) {
                        PathEffect.dashPathEffect(
                            floatArrayOf(20f, 20f), phase = phase.value * 40f
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
        }, contentAlignment = Alignment.Center
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
            imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White
        )
    }
}

@Composable
fun WorkoutTypeSection(
    isPowerSelected: Boolean,
    isCardioSelected: Boolean,
    onPowerClick: () -> Unit,
    onCardioClick: () -> Unit
) {
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
                WorkoutTypeButton(
                    text = "Cardio",
                    isSelected = isCardioSelected,
                    onClick = onCardioClick
                )
                WorkoutTypeButton(
                    text = "Power",
                    isSelected = isPowerSelected,
                    onClick = onPowerClick
                )
            }
        }
    }
}

@Composable
fun WorkoutTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.9f,
        animationSpec = tween(durationMillis = 100),
        label = "alpha"
    )

    val backgroundColor = when {
        isPressed -> Color.Red // Dark Red when pressed
        isSelected -> Color(0xFFFF6666) // Light Red when selected
        else -> Color.LightGray // Default Gray
    }

    val textColor = when {
        isPressed -> Color.White
        isSelected -> Color.White
        else -> Color.Black
    }

    val elevation = when {
        isPressed -> ButtonDefaults.buttonElevation(
            pressedElevation = 8.dp,
            defaultElevation = 2.dp,
            focusedElevation = 4.dp
        )

        else -> ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            focusedElevation = 4.dp
        )
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor.copy(alpha = buttonAlpha),
            contentColor = textColor
        ),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .width(100.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            ),
        interactionSource = interactionSource,
        elevation = elevation
    ) {
        Text(text)
    }
}

@Preview
@Composable
fun FitnessTrackerUI() {
    var activities by remember {
        mutableStateOf(
            listOf(
                "Shoulder Press", "Curls", "Chest Press", "Lateral raises", "Leg raises"
            )
        )
    }
    val allSets = remember { listOf("Set 1", "Set 2", "Set 3", "Set 4", "Set 5", "Set 6") }
    var checkStates by remember { mutableStateOf(List(activities.size) { List(allSets.size) { false } }) }
    var visibleSetsCount by remember { mutableStateOf(allSets.size) }
    var isPowerExpanded by remember { mutableStateOf(false) }
    var isCardioExpanded by remember { mutableStateOf(false) }

    val overallProgress by remember(checkStates, visibleSetsCount) {
        derivedStateOf {
            val totalChecks = checkStates.sumOf { row -> row.take(visibleSetsCount).count { it } }
            val totalPossibleChecks = activities.size * visibleSetsCount
            if (totalPossibleChecks > 0) totalChecks.toFloat() / totalPossibleChecks else 0f
        }
    }

    val density = LocalDensity.current
    val expandedOffset = (-16).dp

    val animatedOffset by animateFloatAsState(
        targetValue = if (isPowerExpanded || isCardioExpanded) with(density) { expandedOffset.toPx() } else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = ""
    )

    val circleSize by animateFloatAsState(
        targetValue = if (isPowerExpanded || isCardioExpanded) 100f else 300f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = ""
    )

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF69B4), Color(0xFFFF8C00), Color(0xFF4169E1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isPowerExpanded) {
                        Modifier.verticalScroll(scrollState)
                    } else {
                        Modifier
                    }
                )
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            TopBar()

            Box(modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, animatedOffset.roundToInt()) }) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ProgressCircle(
                            progress = overallProgress,
                            size = circleSize.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    WorkoutTypeSection(
                        isPowerSelected = isPowerExpanded,
                        isCardioSelected = isCardioExpanded,
                        onPowerClick = {
                            isPowerExpanded = !isPowerExpanded
                            isCardioExpanded = false
                        },
                        onCardioClick = {
                            isCardioExpanded = !isCardioExpanded
                            isPowerExpanded = false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = isPowerExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        PowerTracker(
                            activities = activities,
                            allSets = allSets,
                            checkStates = checkStates,
                            onCheckStateChange = { newCheckStates -> checkStates = newCheckStates },
                            onActivitiesChange = { newActivities ->
                                activities = newActivities
                                checkStates = List(activities.size) { rowIndex ->
                                    checkStates.getOrNull(rowIndex) ?: List(allSets.size) { false }
                                }
                            },
                            onVisibleSetsCountChange = { newVisibleSetsCount ->
                                visibleSetsCount = newVisibleSetsCount
                            },
                            isExpanded = isPowerExpanded
                        )
                    }

                    AnimatedVisibility(
                        visible = isCardioExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        CardioTracker()
                    }
                }
            }
        }

        if (isPowerExpanded) {
            ScrollButtons(
                onScrollUp = {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(0)
                    }
                },
                onScrollDown = {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                alpha = 1f
            )
        }
    }
}

@Composable
fun ScrollButtons(onScrollUp: () -> Unit, onScrollDown: () -> Unit, alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 16.dp)
    ) {
        // Scroll Up Button
        AnimatedScrollButton(
            onClick = onScrollUp,
            icon = Icons.Filled.KeyboardArrowUp,
            contentDescription = "Scroll to top",
            alpha = alpha,
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // Scroll Down Button
        AnimatedScrollButton(
            onClick = onScrollDown,
            icon = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Scroll to bottom",
            alpha = alpha,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 160.dp)
        )
    }
}

@Composable
fun AnimatedScrollButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.8f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.8f,
        animationSpec = tween(durationMillis = 100),
        label = ""
    )

    Box(
        modifier = modifier
            .alpha(alpha * buttonAlpha)
            .graphicsLayer(
                scaleX = scale, scaleY = scale
            )
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = Color.Red.copy(alpha = buttonAlpha),
            contentColor = Color.White,
            interactionSource = interactionSource
        ) {
            Icon(
                imageVector = icon, contentDescription = contentDescription
            )
        }
    }
}



@Composable
fun ProgressCircle(progress: Float, size: Dp) {
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
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "phaseAnimation"
    )

    val density = LocalDensity.current

    Box(
        modifier = Modifier.size(size), contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.toPx()
            val canvasHeight = size.toPx()
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val radius =
                (canvasWidth.coerceAtMost(canvasHeight) - with(density) { 8.dp.toPx() }) / 2f
            val strokeWidth = with(density) { 8.dp.toPx() }

            val sweepAngle = animatedProgress * 360f

            // Draw background circle
            drawCircle(color = backgroundColor,
                radius = if (animatedProgress >= 1f) radius + with(density) { 12.dp.toPx() } else radius,
                center = center,
                style = Stroke(
                    width = strokeWidth, pathEffect = if (animatedProgress >= 1f) {
                        PathEffect.dashPathEffect(
                            floatArrayOf(20f, 20f), phase = phase * 40f
                        )
                    } else null
                ))

            if (animatedProgress >= 1f) {
                drawCircle(
                    color = backgroundColor, radius = radius, center = center, style = Fill
                )
            }

            // Draw progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(with(density) { 4.dp.toPx() }, with(density) { 4.dp.toPx() }),
                size = Size(
                    canvasWidth - with(density) { 8.dp.toPx() },
                    canvasHeight - with(density) { 8.dp.toPx() }),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = (size.value / 5).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Completed", fontSize = (size.value / 10).sp, color = Color.White
            )
        }
    }
}

