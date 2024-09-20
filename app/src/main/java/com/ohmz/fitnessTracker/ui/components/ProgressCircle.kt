package com.ohmz.fitnessTracker.ui.components

import android.util.Log
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ohmz.fitnesstracker.R

@Composable
fun ProgressCircle(workoutProgress: Float, distanceProgress: Float, size: Dp) {
    val animatedWorkoutProgress by animateFloatAsState(
        targetValue = workoutProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "workoutProgressAnimation"
    )

    val animatedDistanceProgress by animateFloatAsState(
        targetValue = distanceProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "distanceProgressAnimation"
    )

    val workoutColor = Color(0xFF56CE5B)  // Green color for workout (outer ring)
    val distanceColor = Color(0xD200E2FF) // Blue color for distance/cardio (inner ring)

    val density = LocalDensity.current

    Box(
        modifier = Modifier.size(size), contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.toPx()
            val canvasHeight = size.toPx()
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            // Calculate radii and stroke width based on the size
            val outerRadius =
                (canvasWidth.coerceAtMost(canvasHeight) - with(density) { 8.dp.toPx() }) / 2f

            // Adjust stroke width and inner radius based on size
            val strokeWidth: Float
            val innerRadius: Float

            if (size < 200.dp) {
                // Shrunk mode: Increase ring thickness
                strokeWidth = outerRadius * 0.4f
                innerRadius = outerRadius - strokeWidth
            } else {
                // Expanded mode: Keep original thickness, make rings touch
                strokeWidth = outerRadius * 0.3f
                innerRadius = outerRadius - strokeWidth
            }

            // Draw outline circles
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

            // Draw workout progress arc
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

            // Draw distance progress arc
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

        // Draw icons
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            // Workout icon (overlapping outer ring)
            Icon(
                painter = painterResource(id = R.drawable.ic_workout),
                contentDescription = "Workout Icon",
                tint = Color.White,
                modifier = Modifier
                    .size(size * 0.13f)
                    .offset(y = -(size * 0.41f))
            )
            Log.d("iconSide", "Running Icon ${-(size * 0.41f)}")

            // Running icon (overlapping inner ring)
            Icon(
                painter = painterResource(id = R.drawable.ic_running),
                contentDescription = "Running Icon",
                tint = Color.White,
                modifier = Modifier
                    .size(size * 0.17f)
                    .offset(
                        y = -(size * 0.27f)
                    )
            )
            Log.d("iconSide", "Running Icon ${-(size * 0.27f)}")

        }
    }
}
