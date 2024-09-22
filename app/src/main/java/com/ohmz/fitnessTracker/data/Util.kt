package com.ohmz.fitnessTracker.data

import android.content.Context

fun getStringResource(context: Context, stringResId: Int): String {
    return context.getString(stringResId)
}