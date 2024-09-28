package com.ohmz.fitnessTracker.confetti.view.component

import com.ohmz.fitnessTracker.confetti.design.PartySystem

interface OnParticleSystemUpdateListener {
    fun onParticleSystemEnded(
        system: PartySystem,
        activeSystems: Int,
    )
}
