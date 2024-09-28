package com.ohmz.fitnessTracker.tracker.views.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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