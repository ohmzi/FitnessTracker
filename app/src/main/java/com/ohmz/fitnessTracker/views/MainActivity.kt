package com.ohmz.fitnessTracker.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ohmz.fitnessTracker.views.screens.FitnessTrackerUI
import com.ohmz.fitnessTracker.views.theme.FitnessTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessTrackerTheme {
                FitnessTrackerUI()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Preview(showBackground = true)
@Composable
fun FitnessTrackerUIPreview() {
    FitnessTrackerTheme {
        FitnessTrackerUI()
    }
}