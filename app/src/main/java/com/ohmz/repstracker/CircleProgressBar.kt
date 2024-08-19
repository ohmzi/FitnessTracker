package com.ohmz.repstracker

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CircleProgressBar() {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = ""
    )
    val ringAnimation = rememberInfiniteTransition(label = "")

    val ringAlpha by ringAnimation.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1000), repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 20.dp.toPx()
                val center = size / 2f
                val radius = ((size.minDimension - strokeWidth) / 2f) + 25

                // Background circle
                drawCircle(
                    color = Color.LightGray, radius = radius, style = Stroke(width = strokeWidth)
                )

                // Progress arc
                drawArc(
                    color = Color.Green,
                    startAngle = -90f,
                    sweepAngle = 360 * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth + 1, cap = StrokeCap.Round)
                )
                if (animatedProgress == 1f) {
                    drawCircle(
                        color = Color.Green.copy(alpha = 1f - ringAlpha),
                        radius = radius + 20.dp.toPx() * ringAlpha,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            Text("${(animatedProgress * 100).toInt()}%")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { progress = 0f }) {
                Text("Reset")
            }
            Button(onClick = { progress = (progress + 0.1f).coerceAtMost(1f) }) {
                Text("Add 10%")
            }
        }
    }
}