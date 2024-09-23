package com.ohmz.fitnessTracker.utils

import android.content.Context

fun getStringResource(context: Context, stringResId: Int): String {
    return context.getString(stringResId)
}