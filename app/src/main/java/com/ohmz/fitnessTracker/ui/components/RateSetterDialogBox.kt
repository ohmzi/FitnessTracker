package com.ohmz.fitnessTracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun RateSetterDialogBox(
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    typeOfWorkout: String
) {
    var selectedValue by remember { mutableStateOf(currentValue) }

    val lightRed = Color(0xFFFFCCCB)
    val darkRed = Color(0xFFFF6961)
    val lightBlue = Color(0xFFADD8E6)
    val darkBlue = Color(0xFF6495ED)
    val lightGreen = Color(0xFF90EE90)
    val darkGreen = Color(0xFF32CD32)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(setDialogTitle(typeOfWorkout), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedButton(onClick = {
                        if (selectedValue > 0.5f) selectedValue -= setDialogStepValue(
                            typeOfWorkout
                        )
                    },
                        lightColor = lightRed,
                        darkColor = darkRed,
                        content = { Text("-", color = Color.Black) })
                    Text(
                        text = setDialogChangeUnit(typeOfWorkout, selectedValue),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    AnimatedButton(onClick = { selectedValue += setDialogStepValue(typeOfWorkout) },
                        lightColor = lightBlue,
                        darkColor = darkBlue,
                        content = { Text("+", color = Color.Black) })
                }

                Spacer(modifier = Modifier.height(16.dp))
                AnimatedButton(onClick = {
                    onValueChange(selectedValue)
                    onDismiss()
                },
                    lightColor = lightGreen,
                    darkColor = darkGreen,
                    modifier = Modifier.fillMaxWidth(),
                    content = { Text("Confirm", color = Color.Black) })
            }
        }
    }
}

fun setDialogStepValue(typeOfWorkout: String): Float = when (typeOfWorkout) {
    "Cardio" -> 0.5f
    "Power" -> 5f
    else -> 0.0f
}


fun setDialogChangeUnit(typeOfWorkout: String, selectedValue: Float): String =
    when (typeOfWorkout) {
        "Cardio" -> String.format("%.1f km", selectedValue)
        "Power" -> selectedValue.toString()
        else -> "units"
    }

fun setDialogTitle(typeOfWorkout: String): String =
    when (typeOfWorkout) {
        "Cardio" -> "Select Target Distance"
        "Power" -> "Change Weight Value"
        else -> "units"
    }


@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    lightColor: Color,
    darkColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        )
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) darkColor else lightColor,
        animationSpec = tween(durationMillis = 100)
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer(
            scaleX = scale, scaleY = scale
        )
    ) {
        content()
    }
}

