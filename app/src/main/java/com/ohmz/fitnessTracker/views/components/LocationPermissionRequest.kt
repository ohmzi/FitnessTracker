package com.ohmz.fitnessTracker.views.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.ohmz.fitnessTracker.utils.getStringResource
import com.ohmz.fitnesstracker.R

@Composable
fun LocationPermissionRequest(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                onPermissionResult(isGranted)
            })

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionResult(true)
            }

            shouldShowRequestPermissionRationale(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showRationale = true
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    if (showRationale) {
        AlertDialog(onDismissRequest = { showRationale = false },
            title = {
                Text(
                    getStringResource(
                        context = context,
                        stringResId = R.string.map_locationPermissionNeededDialogTitle
                    )
                )
            },
            text = {
                Text(
                    getStringResource(
                        context = context,
                        stringResId = R.string.map_locationPermissionNeededDialogBody
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text(
                        getStringResource(
                            context = context,
                            stringResId = R.string.map_locationPermissionGranted
                        )
                    )
                }
            },
            dismissButton = {
                Button(onClick = {
                    showRationale = false
                    onPermissionResult(false)
                }) {

                    Text(
                        getStringResource(
                            context = context,
                            stringResId = R.string.map_locationPermissionDenied
                        )
                    )
                }
            })
    }
}

private fun shouldShowRequestPermissionRationale(
    context: android.content.Context, permission: String
): Boolean {
    return if (context is androidx.activity.ComponentActivity) {
        context.shouldShowRequestPermissionRationale(permission)
    } else {
        false
    }
}