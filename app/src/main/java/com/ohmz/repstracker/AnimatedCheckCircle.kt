package com.ohmz.repstracker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCheckCircle() {
    var isChecked by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedColor by animateColorAsState(
        targetValue = if (isChecked) Color.Green else Color.Gray,
        animationSpec = tween(durationMillis = 300)
    )

    val ringAnimation = rememberInfiniteTransition()
    val ringAlpha by ringAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(60.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null // Remove default ripple
                ) { isChecked = !isChecked },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(60.dp)) {
                val center = this.center
                val radius = size.minDimension / 2

                // Draw shadow if pressed
                if (isPressed) {
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = radius - 2.dp.toPx(),
                        center = center.copy(y = center.y + 2.dp.toPx())
                    )
                }

                // Draw the main circle
                drawCircle(
                    color = animatedColor,
                    radius = radius
                )

                // Draw the animated ring
                if (isChecked) {
                    drawCircle(
                        color = Color.Green.copy(alpha = 1f - ringAlpha),
                        radius = radius + 5.dp.toPx() * ringAlpha,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checkmark",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}