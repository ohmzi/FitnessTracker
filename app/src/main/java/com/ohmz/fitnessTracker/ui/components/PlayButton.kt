package com.ohmz.fitnessTracker.ui.components

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ohmz.fitnessTracker.data.getStringResource
import com.ohmz.fitnessTracker.ui.theme.weightButtonChecked
import com.ohmz.fitnesstracker.R

@Composable
fun PlayButton(isTracking: Boolean, onToggle: (Boolean) -> Unit) {
    val context = LocalContext.current
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

    val backgroundColor =
        if (isTracking) Color.Red else weightButtonChecked // Green when not tracking

    Box(
        modifier = Modifier
            .size(140.dp)
            .graphicsLayer(
                scaleX = scale, scaleY = scale, alpha = buttonAlpha
            )
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                onToggle(!isTracking)
                Toast.makeText(
                    context,
                    if (!isTracking)
                        getStringResource(context = context, stringResId = R.string.toast_startRun)
                    else getStringResource(context = context, stringResId = R.string.toast_stopRun),
                    Toast.LENGTH_SHORT
                ).show()
            },
            interactionSource = interactionSource,
            modifier = Modifier.size(140.dp)
        ) {
            Icon(
                imageVector = if (isTracking) Icons.Default.Menu else Icons.Default.PlayArrow,
                contentDescription = if (isTracking)
                    getStringResource(context = context, stringResId = R.string.playButton_stop)
                else getStringResource(context = context, stringResId = R.string.playButton_start),
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}
