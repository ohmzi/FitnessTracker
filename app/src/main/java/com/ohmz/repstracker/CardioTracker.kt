package com.ohmz.repstracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun CardioTracker() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight() // This will make the card take up all available vertical space
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween, // This will spread out the content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RunningStats()

            Distance()

            PlayButton()

            Spacer(modifier = Modifier.height(24.dp)) // Add some space at the bottom
        }
    }
}

@Composable
fun RunningStats() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "00:00",
            color = Color.Red,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Time",
            color = Color.Red,
            fontSize = 18.sp
        )
    }
}

@Composable
fun Distance() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "0.0",
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            fontStyle = FontStyle.Italic
        )
        Text(
            "Kilometres",
            fontSize = 24.sp,
            color = Color.Red
        )
    }
}


@Composable
fun PlayButton() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(Color.Red, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(72.dp)
        )
    }
}