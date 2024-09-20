package com.ohmz.fitnessTracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ohmz.fitnessTracker.ui.theme.weightButtonChecked
import com.ohmz.fitnessTracker.ui.theme.workoutExerciseLabel

@Composable
fun IndividualWorkoutButton(
    label: String, progress: Float, modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    val outlineColor by animateColorAsState(
        targetValue = if (progress >= 1f) weightButtonChecked else Color.White,
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
        .background(workoutExerciseLabel)
        .drawWithContent {
            val cornerRadius = 16.dp.toPx()

            drawRoundRect(
                color = weightButtonChecked,
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