package com.ohmz.fitnessTracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable

fun WeightButtons(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    size: Dp = 40.dp,
    label: String,
    onLabelChange: (String) -> Unit
) {
    var showEditPopup by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        if (isChecked) Color(0xFF4CAF50) else Color(0xFF37474F), label = "backgroundColor"
    )
    val cornerRadius = size / 4

    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(bounded = true, radius = size / 2)

    val squareAnimation = rememberInfiniteTransition(label = "squareAnimation")
    val squareAlpha by squareAnimation.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1000), repeatMode = RepeatMode.Reverse
        ), label = "squareAlpha"
    )

    Box(modifier = Modifier
        .size(size)
        .drawBehind {
            if (isChecked) {
                val animatedSize = size.toPx() + 10.dp.toPx() * squareAlpha
                val offset = (animatedSize - size.toPx()) / 2

                drawRoundRect(
                    color = Color(0xFF4CAF50).copy(alpha = 1f - squareAlpha),
                    topLeft = Offset(-offset, -offset),
                    size = Size(animatedSize, animatedSize),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        .background(backgroundColor, RoundedCornerShape(cornerRadius))
        .indication(interactionSource, indication)
        .combinedClickable(interactionSource = interactionSource,
            indication = null,
            onClick = { onCheckedChange(!isChecked) },
            onLongClick = { if (!isChecked) showEditPopup = true }),
        contentAlignment = Alignment.Center
    ) {
        if (!isChecked) {
            Text(
                text = label,
                color = Color.White,
                fontSize = (size.value * 0.3).sp,
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }

    if (showEditPopup) {
        RateSetterDialogBox(currentValue = label.toFloat(), onValueChange = { newValue ->
            onLabelChange(newValue.toInt().toString())
            showEditPopup = false
        }, onDismiss = { showEditPopup = false }, typeOfWorkout = "Power"
        )
    }
}