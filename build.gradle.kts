// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.mapsplatform.secrets) apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false


}

buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath(libs.android.mapsplatform.secrets)
        classpath(libs.com.google.devtools.ksp.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)

    }
}