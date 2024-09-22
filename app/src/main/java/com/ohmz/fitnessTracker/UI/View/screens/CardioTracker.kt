package com.ohmz.fitnessTracker.UI.View.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.ohmz.fitnessTracker.UI.View.components.Distance
import com.ohmz.fitnessTracker.UI.View.components.GoogleMapComponent
import com.ohmz.fitnessTracker.UI.View.components.LocationPermissionRequest
import com.ohmz.fitnessTracker.UI.View.components.PlayButton
import com.ohmz.fitnessTracker.UI.View.components.RunningStats
import com.ohmz.fitnessTracker.Utils.getStringResource
import com.ohmz.fitnesstracker.R
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
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
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
            GoogleMapComponent(hasLocationPermission = true)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    getStringResource(
                        context = context,
                        stringResId = R.string.map_locationPermissionNeeded
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                RunningStats(
                    time = time,
                    pace = if (pace.isFinite()) String.format("%.2f", pace) else "0.00"
                )
                Distance(
                    distance = distance / 1000f, // Convert to km
                    targetDistance = targetDistance, onTargetDistanceChange = onTargetDistanceChange
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
            PlayButton(isTracking = isTracking, onToggle = { newIsTracking ->
                isTracking = newIsTracking
                if (!newIsTracking) {
                    // Reset values when stopping
                    distance = 0f
                    time = 0L
                    pace = 0f
                    lastLocation = null
                }
            })
        }
    }
}

