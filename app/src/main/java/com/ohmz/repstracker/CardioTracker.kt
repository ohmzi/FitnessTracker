package com.ohmz.repstracker

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState

@Preview
@Composable
fun CardioTracker() {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map as background
        GoogleMap(hasLocationPermission)

        // Overlay with CardioTracker UI
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RunningStats()
            Distance()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(70.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            PlayButton()
        }
    }
}

@Composable
fun GoogleMap(hasLocationPermission: Boolean) {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
    )
}

@Composable
fun RunningStats() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Time",
            color = Color.Red,
            fontSize = 18.sp
        )
        Text(
            "00:00",
            color = Color.Red,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Distance (Km)",
            color = Color.Red,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 10.dp)
        )

    }
}

@Composable
fun Distance() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                "0.0",
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                lineHeight = 80.sp,
                modifier = Modifier.offset(y = (-14).dp)  // Reduced line height
            )
            Text(
                "/",
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                lineHeight = 80.sp,
                modifier = Modifier.offset(y = (-14).dp)  // Reduced line height
            )
            Text(
                "5.0",
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                lineHeight = 80.sp,
                modifier = Modifier.offset(y = (-14).dp), // Reduced line height
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                "Progress",
                fontSize = 18.sp,
                color = Color.Red,
            )
            Text(
                "",
                fontSize = 18.sp,
                color = Color.Red,
            )
            Text(
                "Target",
                fontSize = 18.sp,
                color = Color.Red,
            )
        }
    }
}

@Composable
fun PlayButton() {
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
        if (isPressed) Color.Red else Color(0xFFFF6666) // Light red when not pressed

    Box(
        modifier = Modifier
            .size(140.dp) // Increased size to accommodate larger icon
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = buttonAlpha
            )
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                Toast.makeText(context, "Start the run now", Toast.LENGTH_SHORT).show()
            },
            interactionSource = interactionSource,
            modifier = Modifier.size(140.dp) // Match the size of the Box
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(100.dp) // Increased icon size
            )
        }
    }
}