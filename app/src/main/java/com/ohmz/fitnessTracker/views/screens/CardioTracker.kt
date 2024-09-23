package com.ohmz.fitnessTracker.views.screens

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
import com.ohmz.fitnessTracker.utils.getStringResource
import com.ohmz.fitnessTracker.views.components.Distance
import com.ohmz.fitnessTracker.views.components.GoogleMapComponent
import com.ohmz.fitnessTracker.views.components.LocationPermissionRequest
import com.ohmz.fitnessTracker.views.components.PlayButton
import com.ohmz.fitnessTracker.views.components.RunningStats
import com.ohmz.fitnesstracker.R
import kotlinx.coroutines.delay

@Composable
fun CardioTracker(
    distances: Float,
    onDistanceChange: (Float) -> Unit,
    targetDistance: Float,
    onTargetDistanceChange: (Float) -> Unit,
    time: Long,
    onTimeChange: (Long) -> Unit,
    pace: Float,
    onPaceChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isTracking by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                if (lastLocation != null) {
                    val newDistance = lastLocation!!.distanceTo(location)
                    onDistanceChange(distances + newDistance)
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
                    return@LaunchedEffect
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }

            // Start a coroutine for the timer
            val startTime = SystemClock.elapsedRealtime() - time // Resume from previous time if any
            while (isTracking) {
                delay(1000) // Update every second
                val newTime = SystemClock.elapsedRealtime() - startTime
                onTimeChange(newTime)
                // Calculate pace (minutes per kilometer)
                if (distances > 0) {
                    val newPace = (newTime / 60000f) / (distances / 1000f)
                    onPaceChange(newPace)
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                RunningStats(
                    time = time,
                    pace = if (pace.isFinite()) String.format("%.2f", pace) else "0.00"
                )
                Distance(
                    distance = distances / 1000f, // Convert to km
                    targetDistance = targetDistance,
                    onTargetDistanceChange = onTargetDistanceChange
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
            PlayButton(
                isTracking = isTracking,
                onToggle = { newIsTracking ->
                    isTracking = newIsTracking
                    if (!newIsTracking) {
                        onReset()
                        lastLocation = null
                    }
                }
            )
        }
    }
}