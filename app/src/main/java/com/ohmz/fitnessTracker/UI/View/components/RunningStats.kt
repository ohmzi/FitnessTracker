package com.ohmz.fitnessTracker.UI.View.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmz.fitnessTracker.Utils.Values
import com.ohmz.fitnessTracker.Utils.getStringResource
import com.ohmz.fitnesstracker.R

@Composable
fun RunningStats(
    time: Long, pace: String
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)
            ) {
                Text(
                    getStringResource(context = context, stringResId = R.string.runningStat_Time),
                    color = Color.Red,
                    fontSize = 18.sp,
                )
                Text(
                    text = formatTime(time),
                    color = Color.Red,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        getStringResource(
                            context = context, stringResId = R.string.runningStat_Pace
                        ), color = Color.Red, fontSize = 18.sp
                    )
                    Text(
                        " (mins/km)", color = Color.Red, fontSize = 14.sp
                    )
                }
                Text(
                    text = pace, color = Color.Red, fontSize = 30.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            getStringResource(context = context, stringResId = R.string.runningStat_Distance),
            color = Color.Red,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun Distance(
    distance: Float,
    targetDistance: Float,
    onTargetDistanceChange: (Float) -> Unit
) {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Row(
            Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
        ) {
            val distances = Math.round(distance * 10) / 10.0
            Text(
                text = distances.toString(),
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                lineHeight = 80.sp,
                modifier = Modifier.offset(y = (-14).dp)
            )
            Text(
                "/",
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                lineHeight = 80.sp,
                modifier = Modifier.offset(y = (-14).dp)
            )
            Text(text = String.format("%.1f", targetDistance),
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                lineHeight = 80.sp,
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .clickable { showPicker = true })
        }
        Row(
            Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                getStringResource(context = context, stringResId = R.string.runningStat_Progress),
                fontSize = 18.sp,
                color = Color.Red,
            )
            Text(
                "",
                fontSize = 18.sp,
                color = Color.Red,
            )
            Text(
                getStringResource(context = context, stringResId = R.string.runningStat_Target),
                fontSize = 18.sp,
                color = Color.Red,
            )
        }
    }

    if (showPicker) {
        RateSetterDialogBox(
            currentValue = targetDistance,
            onValueChange = { newValue -> onTargetDistanceChange(newValue) },
            onDismiss = { showPicker = false },
            typeOfWorkout = Values.WORKOUT_CARDIO
        )
    }
}

fun formatTime(timeInMillis: Long): String {
    val seconds = (timeInMillis / 1000) % 60
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}



