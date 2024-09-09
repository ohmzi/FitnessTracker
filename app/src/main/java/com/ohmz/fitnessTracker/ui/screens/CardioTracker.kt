@file:OptIn(ExperimentalFoundationApi::class)

package com.ohmz.fitnessTracker.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun CardioTracker(
    distances: Float,
    onDistanceChange: (Float) -> Unit,
    targetDistance: Float,
    onTargetDistanceChange: (Float) -> Unit
) {
    var distance = distances
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isTracking by remember { mutableStateOf(false) }
    var time by remember { mutableLongStateOf(0L) } // in milliseconds
    var pace by remember { mutableFloatStateOf(0f) } // in minutes per kilometer

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                if (lastLocation != null) {
                    val newDistance = lastLocation!!.distanceTo(location)
                    onDistanceChange(distance + newDistance)
                }
                lastLocation = location
            }
        }
    }

    LaunchedEffect(isTracking) {
        if (isTracking) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 2000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            if (hasLocationPermission) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@LaunchedEffect
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }

            // Start a coroutine for the timer
            launch {
                val startTime =
                    SystemClock.elapsedRealtime() - time // Resume from previous time if any
                while (isTracking) {
                    delay(1000) // Update every second
                    time = SystemClock.elapsedRealtime() - startTime
                    // Calculate pace (minutes per kilometer)
                    if (distance > 0) {
                        pace = (time / 60000f) / (distance / 1000f)
                    }
                }
            }
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LocationPermissionRequest { granted ->
            hasLocationPermission = granted
        }

        if (hasLocationPermission) {
            GoogleMap(hasLocationPermission = true)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Location permission is required to track your run.")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                RunningStatss(
                    time = formatTime(time),
                    pace = if (pace.isFinite()) String.format("%.2f", pace) else "0.00"
                )
                Distance(
                    distance = distance / 1000f, // Convert to km
                    targetDistance = targetDistance,
                    onTargetDistanceChange = onTargetDistanceChange
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            PlayButton(
                isTracking = isTracking,
                onToggle = { newIsTracking ->
                    isTracking = newIsTracking
                    if (!newIsTracking) {
                        // Reset values when stopping
                        distance = 0f
                        time = 0L
                        pace = 0f
                        lastLocation = null
                    }
                }
            )
        }
    }
}

@Composable
fun RunningStatss(time: String, pace: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Time",
                    color = Color.Red,
                    fontSize = 18.sp,
                )
                Text(
                    text = time,
                    color = Color.Red,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Pace",
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                    Text(
                        " (mins/km)",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = pace,
                    color = Color.Red,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            "Distance (Km)",
            color = Color.Red,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}


@Composable
fun DistancePickerPopup(
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    onDismiss: () -> Unit
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
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Target Distance", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedButton(
                        onClick = { if (selectedValue > 0.5f) selectedValue -= 0.5f },
                        lightColor = lightRed,
                        darkColor = darkRed,
                        content = { Text("-", color = Color.Black) }
                    )
                    Text(
                        text = String.format("%.1f km", selectedValue),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    AnimatedButton(
                        onClick = { selectedValue += 0.5f },
                        lightColor = lightBlue,
                        darkColor = darkBlue,
                        content = { Text("+", color = Color.Black) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                AnimatedButton(
                    onClick = {
                        onValueChange(selectedValue)
                        onDismiss()
                    },
                    lightColor = lightGreen,
                    darkColor = darkGreen,
                    modifier = Modifier.fillMaxWidth(),
                    content = { Text("Confirm", color = Color.Black) }
                )
            }
        }
    }
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
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
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
            scaleX = scale,
            scaleY = scale
        )
    ) {
        content()
    }
}

@Composable
fun Distance(
    distance: Float,
    targetDistance: Float,
    onTargetDistanceChange: (Float) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
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
            Text(
                text = String.format("%.1f", targetDistance),
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                lineHeight = 80.sp,
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .clickable { showPicker = true }
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

    if (showPicker) {
        DistancePickerPopup(
            currentValue = targetDistance,
            onValueChange = { newValue -> onTargetDistanceChange(newValue) },
            onDismiss = { showPicker = false }
        )
    }
}

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
        if (isTracking) Color.Red else Color(0xFF4CAF50) // Green when not tracking

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
                    if (!isTracking) "Starting the run" else "Stopping the run",
                    Toast.LENGTH_SHORT
                ).show()
            },
            interactionSource = interactionSource,
            modifier = Modifier.size(140.dp)
        ) {
            Icon(
                imageVector = if (isTracking) Icons.Default.Menu else Icons.Default.PlayArrow,
                contentDescription = if (isTracking) "Stop" else "Start",
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}


fun formatTime(timeInMillis: Long): String {
    val seconds = (timeInMillis / 1000) % 60
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
fun GoogleMap(hasLocationPermission: Boolean) {
    var isMapLoaded by remember { mutableStateOf(false) }
    var mapLoadError by remember { mutableStateOf<String?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("GoogleMap", "Error getting location", e)
            }
        } else {
            Log.w("GoogleMap", "Location permission not granted")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true
            ),
            onMapLoaded = {
                isMapLoaded = true
                Log.d("GoogleMap", "Map loaded successfully")
            }
        ) {
            currentLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Current Location",
                    snippet = "You are here"
                )
            }
        }

        if (!isMapLoaded) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        mapLoadError?.let { error ->
            Text(
                text = "Error loading map: $error",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!isMapLoaded) {
                mapLoadError = "Map failed to load"
                Log.e("GoogleMap", "Map failed to load")
            }
        }
    }
}

@Composable
fun LocationPermissionRequest(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onPermissionResult(isGranted)
        }
    )

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionResult(true)
            }

            shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showRationale = true
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location Permission Required") },
            text = {
                Text(
                    "This app needs access to your precise location to show it on the map. " +
                            "Please grant the location permission."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationale = false
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showRationale = false
                        onPermissionResult(false)
                    }
                ) {
                    Text("Deny")
                }
            }
        )
    }
}

private fun shouldShowRequestPermissionRationale(
    context: android.content.Context,
    permission: String
): Boolean {
    return if (context is androidx.activity.ComponentActivity) {
        context.shouldShowRequestPermissionRationale(permission)
    } else {
        false
    }
}