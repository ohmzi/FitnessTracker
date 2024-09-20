package com.ohmz.fitnessTracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ohmz.fitnessTracker.ui.components.ProgressCircle
import com.ohmz.fitnessTracker.ui.components.ScrollButtons
import com.ohmz.fitnessTracker.ui.components.TopBar
import com.ohmz.fitnessTracker.ui.components.WorkoutTypeSection
import com.ohmz.fitnessTracker.ui.theme.BackgroundColor1
import com.ohmz.fitnessTracker.ui.theme.BackgroundColor2
import com.ohmz.fitnessTracker.ui.theme.BackgroundColor3
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    // New state for distance and target distance
    var distance by remember { mutableStateOf(0f) }
    var targetDistance by remember { mutableStateOf(5f) }

    val workoutProgress by remember(checkStates, visibleSetsCount) {
        derivedStateOf {
            val totalChecks = checkStates.sumOf { row -> row.take(visibleSetsCount).count { it } }
            val totalPossibleChecks = activities.size * visibleSetsCount
            if (totalPossibleChecks > 0) totalChecks.toFloat() / totalPossibleChecks else 0f
        }
    }

    val distanceProgress by remember((distance / 1000), targetDistance) {
        derivedStateOf { ((distance / 1000) / targetDistance).coerceIn(0f, 1f) }
    }

    val density = LocalDensity.current
    val expandedOffset = (-16).dp

    val animatedOffset by animateFloatAsState(targetValue = if (isPowerExpanded || isCardioExpanded) with(
        density
    ) { expandedOffset.toPx() } else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "")

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
                        BackgroundColor1, BackgroundColor2, BackgroundColor3
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
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        ProgressCircle(
                            workoutProgress = workoutProgress,
                            distanceProgress = distanceProgress,
                            size = circleSize.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    WorkoutTypeSection(isPowerSelected = isPowerExpanded,
                        isCardioSelected = isCardioExpanded,
                        onPowerClick = {
                            isPowerExpanded = !isPowerExpanded
                            isCardioExpanded = false
                        },
                        onCardioClick = {
                            isCardioExpanded = !isCardioExpanded
                            isPowerExpanded = false
                        })

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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            CardioTracker(distances = distance,
                                onDistanceChange = { distance = it },
                                targetDistance = targetDistance,
                                onTargetDistanceChange = { targetDistance = it })
                        }
                    }
                }
            }
        }

        if (isPowerExpanded) {
            ScrollButtons(onScrollUp = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(0)
                }
            }, onScrollDown = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }, alpha = 1f
            )
        }
    }
}




