package com.ohmz.fitnessTracker.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ohmz.fitnessTracker.data.getStringResource
import com.ohmz.fitnessTracker.ui.theme.distanceColor
import com.ohmz.fitnessTracker.ui.theme.workoutColor
import com.ohmz.fitnesstracker.R

@Composable
fun ProgressCircle(workoutProgress: Float, distanceProgress: Float, size: Dp) {
    val animatedWorkoutProgress by animateFloatAsState(
        targetValue = workoutProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "workoutProgressAnimation"
    )

    val context = LocalContext.current
    val animatedDistanceProgress by animateFloatAsState(
        targetValue = distanceProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "distanceProgressAnimation"
    )

    val density = LocalDensity.current

    Box(
        modifier = Modifier.size(size), contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.toPx()
            val canvasHeight = size.toPx()
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            val outerRadius =
                (canvasWidth.coerceAtMost(canvasHeight) - with(density) { 8.dp.toPx() }) / 2f

            val strokeWidth: Float
            val innerRadius: Float

            if (size < 200.dp) {
                strokeWidth = outerRadius * 0.4f
                innerRadius = outerRadius - strokeWidth
            } else {
                strokeWidth = outerRadius * 0.3f
                innerRadius = outerRadius - strokeWidth
            }

            drawCircle(
                color = workoutColor.copy(alpha = 0.5f),
                radius = outerRadius - strokeWidth / 2,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            drawCircle(
                color = distanceColor.copy(alpha = 0.3f),
                radius = innerRadius - strokeWidth / 2,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = workoutColor,
                startAngle = -90f,
                sweepAngle = animatedWorkoutProgress * 360f,
                useCenter = false,
                topLeft = Offset(
                    center.x - outerRadius + strokeWidth / 2,
                    center.y - outerRadius + strokeWidth / 2
                ),
                size = Size(
                    (outerRadius - strokeWidth / 2) * 2, (outerRadius - strokeWidth / 2) * 2
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = distanceColor,
                startAngle = -90f,
                sweepAngle = animatedDistanceProgress * 360f,
                useCenter = false,
                topLeft = Offset(
                    center.x - innerRadius + strokeWidth / 2,
                    center.y - innerRadius + strokeWidth / 2
                ),
                size = Size(
                    (innerRadius - strokeWidth / 2) * 2, (innerRadius - strokeWidth / 2) * 2
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_workout),
                contentDescription = getStringResource(
                    context = context,
                    stringResId = R.string.progressCircle_description_Workout
                ),
                tint = Color.White,
                modifier = Modifier
                    .size(size * 0.13f)
                    .offset(y = -(size * 0.41f))
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_running),
                contentDescription = getStringResource(
                    context = context,
                    stringResId = R.string.progressCircle_description_Running
                ),
                tint = Color.White,
                modifier = Modifier
                    .size(size * 0.17f)
                    .offset(
                        y = -(size * 0.27f)
                    )
            )
        }
    }
}
