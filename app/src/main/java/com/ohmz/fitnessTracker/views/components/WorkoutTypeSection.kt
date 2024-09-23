package com.ohmz.fitnessTracker.views.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ohmz.fitnessTracker.utils.getStringResource
import com.ohmz.fitnessTracker.views.theme.WorkoutTypeSelected
import com.ohmz.fitnesstracker.R

@Composable
fun WorkoutTypeSection(
    isPowerSelected: Boolean,
    isCardioSelected: Boolean,
    onPowerClick: () -> Unit,
    onCardioClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getStringResource(context = context, stringResId = R.string.workoutSelector),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutTypeButton(
                    getStringResource(
                        context = context,
                        stringResId = R.string.workoutSelector_Cardio
                    ),
                    isSelected = isCardioSelected,
                    onClick = onCardioClick
                )
                WorkoutTypeButton(
                    getStringResource(
                        context = context,
                        stringResId = R.string.workoutSelector_Power
                    ),
                    isSelected = isPowerSelected,
                    onClick = onPowerClick
                )
            }
        }
    }
}

@Composable
fun WorkoutTypeButton(
    text: String, isSelected: Boolean, onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.9f,
        animationSpec = tween(durationMillis = 100),
        label = "alpha"
    )

    val backgroundColor = when {
        isPressed -> Color.Red // Dark Red when pressed
        isSelected -> WorkoutTypeSelected // Light Red when selected
        else -> Color.LightGray // Default Gray
    }

    val textColor = when {
        isPressed -> Color.White
        isSelected -> Color.White
        else -> Color.Black
    }

    val elevation = when {
        isPressed -> ButtonDefaults.buttonElevation(
            pressedElevation = 8.dp, defaultElevation = 2.dp, focusedElevation = 4.dp
        )

        else -> ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp, pressedElevation = 8.dp, focusedElevation = 4.dp
        )
    }

    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor.copy(alpha = buttonAlpha), contentColor = textColor
        ), shape = RoundedCornerShape(50), modifier = Modifier
            .width(100.dp)
            .graphicsLayer(
                scaleX = scale, scaleY = scale
            ), interactionSource = interactionSource, elevation = elevation
    ) {
        Text(text)
    }
}