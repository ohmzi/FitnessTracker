package com.ohmz.fitnessTracker.confetti.view

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ohmz.fitnessTracker.confetti.ConfettiViewModel
import com.ohmz.fitnessTracker.confetti.design.PartySystem
import com.ohmz.fitnessTracker.confetti.view.component.OnParticleSystemUpdateListener
import com.ohmz.fitnesstracker.R
import com.ohmz.tictactoe.confetti.view.KonfettiView

@Composable
fun ConfettiUI(viewModel: ConfettiViewModel = ConfettiViewModel()) {
    val state: ConfettiViewModel.State by viewModel.state.observeAsState(
        ConfettiViewModel.State.Idle,
    )
    val drawable = AppCompatResources.getDrawable(LocalContext.current, R.drawable.ic_heart)
    when (val newState = state) {
        ConfettiViewModel.State.Idle -> {
            Column(
                modifier =
                Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                viewModel.explode()

            }
        }

        is ConfettiViewModel.State.Started ->
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = newState.party,
                updateListener =
                object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(
                        system: PartySystem,
                        activeSystems: Int,
                    ) {
                        if (activeSystems == 0) viewModel.ended()
                    }
                },
            )
    }
}
