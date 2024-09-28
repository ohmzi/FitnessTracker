package com.ohmz.fitnessTracker.tracker.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ohmz.fitnessTracker.confetti.ConfettiViewModel
import com.ohmz.fitnessTracker.tracker.views.screens.FitnessTrackerUI
import com.ohmz.fitnessTracker.tracker.views.theme.FitnessTrackerTheme

class MainActivity : ComponentActivity() {
    private val viewModelC by viewModels<ConfettiViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessTrackerTheme {
                FitnessTrackerUI(confettiViewModel = viewModelC)
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
        //FitnessTrackerUI(confettiViewModel = viewModelC)
    }
}