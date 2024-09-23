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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ohmz.fitnessTracker.ui.components.ProgressCircle
import com.ohmz.fitnessTracker.ui.components.ScrollButtons
import com.ohmz.fitnessTracker.ui.components.TopBar
import com.ohmz.fitnessTracker.ui.components.WorkoutTypeSection
import com.ohmz.fitnessTracker.ui.theme.BackgroundColor1
import com.ohmz.fitnessTracker.ui.theme.BackgroundColor2
import com.ohmz.fitnessTracker.ui.theme.BackgroundColor3
import com.ohmz.fitnessTracker.viewModel.FitnessTrackerViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FitnessTrackerUI(viewModel: FitnessTrackerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val workoutProgress =
        remember(uiState.powerWorkout.checkStates, uiState.powerWorkout.visibleSetsCount) {
            val totalChecks = uiState.powerWorkout.checkStates.sumOf { row ->
                row.take(uiState.powerWorkout.visibleSetsCount).count { it }
            }
            val totalPossibleChecks =
                uiState.powerWorkout.activities.size * uiState.powerWorkout.visibleSetsCount
            if (totalPossibleChecks > 0) totalChecks.toFloat() / totalPossibleChecks else 0f
    }

    val distanceProgress =
        (uiState.cardioWorkout.distance / 1000 / uiState.cardioWorkout.targetDistance).coerceIn(
            0f,
            1f
        )

    val density = LocalDensity.current
    val expandedOffset = (-16).dp

    val animatedOffset by animateFloatAsState(
        targetValue = if (uiState.isPowerExpanded || uiState.isCardioExpanded) with(density) { expandedOffset.toPx() } else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = ""
    )

    val circleSize by animateFloatAsState(
        targetValue = if (uiState.isPowerExpanded || uiState.isCardioExpanded) 100f else 300f,
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
                    if (uiState.isPowerExpanded) {
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
                            workoutProgress = workoutProgress,
                            distanceProgress = distanceProgress,
                            size = circleSize.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    WorkoutTypeSection(
                        isPowerSelected = uiState.isPowerExpanded,
                        isCardioSelected = uiState.isCardioExpanded,
                        onPowerClick = { viewModel.togglePowerExpanded() },
                        onCardioClick = { viewModel.toggleCardioExpanded() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = uiState.isPowerExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        PowerTracker(
                            activities = uiState.powerWorkout.activities,
                            allSets = uiState.powerWorkout.allSets,
                            checkStates = uiState.powerWorkout.checkStates,
                            onCheckStateChange = { viewModel.updatePowerWorkoutCheckState(it) },
                            onActivitiesChange = { viewModel.updatePowerWorkoutActivities(it) },
                            onVisibleSetsCountChange = {
                                viewModel.updatePowerWorkoutVisibleSetsCount(
                                    it
                                )
                            },
                            isExpanded = uiState.isPowerExpanded,
                            labelStates = uiState.powerWorkout.labelStates,
                            onLabelChange = { viewModel.updatePowerWorkoutLabelStates(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = uiState.isCardioExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            CardioTracker(
                                distances = uiState.cardioWorkout.distance,
                                onDistanceChange = { viewModel.updateCardioWorkoutDistance(it) },
                                targetDistance = uiState.cardioWorkout.targetDistance,
                                onTargetDistanceChange = {
                                    viewModel.updateCardioWorkoutTargetDistance(
                                        it
                                    )
                                },
                                time = uiState.cardioWorkout.time,
                                onTimeChange = { viewModel.updateCardioWorkoutTime(it) },
                                pace = uiState.cardioWorkout.pace,
                                onPaceChange = { viewModel.updateCardioWorkoutPace(it) },
                                onReset = { viewModel.resetCardioWorkout() }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isPowerExpanded) {
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