package com.ohmz.fitnessTracker.Data.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cardio_sessions")
data class CardioSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "distance") val distance: Float,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "date") val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "power_lifting_sessions")
data class PowerLiftingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exercise: String,
    val weight: Int,
    val reps: Int,
    val sets: Int,
    val date: Long = System.currentTimeMillis()
)