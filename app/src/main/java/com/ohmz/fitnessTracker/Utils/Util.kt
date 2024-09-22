package com.ohmz.fitnessTracker.Utils

import android.content.Context

fun getStringResource(context: Context, stringResId: Int): String {
    return context.getString(stringResId)
}