package com.ohmz.repstracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun FitnessTrackerUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF69B4),  // Pink
                        Color(0xFFFF8C00),  // Dark Orange
                        Color(0xFF4169E1)   // Royal Blue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            TopBar()
            Spacer(modifier = Modifier.height(24.dp))
            ProgressCircle()
            Spacer(modifier = Modifier.height(16.dp))
            WorkoutTypeSection()
            ActivityGrid()
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
        )
        Text(
            text = "Current Progress",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White
        )
    }
}

@Composable
fun ProgressCircle() {
    Box(
        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 0.75f },
            modifier = Modifier.size(100.dp),
            color = Color.Red,
            strokeWidth = 8.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "75%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White
            )
            Text(
                text = "Completed", fontSize = 10.sp, color = Color.White
            )
        }
    }
}

@Composable
fun WorkoutTypeSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Workout Type", fontWeight = FontWeight.Bold, color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutTypeButton("Cardio", isSelected = false)
                WorkoutTypeButton("Power", isSelected = true)
            }
        }
    }
}

@Composable
fun WorkoutTypeButton(text: String, isSelected: Boolean) {
    Button(
        onClick = { /* Handle click */ }, colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Red else Color.LightGray
        ), shape = RoundedCornerShape(50), modifier = Modifier.width(100.dp)
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Black)
    }
}

@Composable
fun WeeklyProgressBar() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActivityGrid()

        }
    }
}

@Composable
fun AddButton() {
    Box(
        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        FloatingActionButton(
            onClick = { /* Handle click */ }, containerColor = Color.Red, contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }
    }
}