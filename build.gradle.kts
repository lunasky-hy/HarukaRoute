// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.serialization) apply false
}

allprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")) {
            configurations.named("implementation") {
                exclude(group = "com.google.android.gms", module = "play-services-maps")

            }
        }
    }
}

buildscript {
    dependencies {
        classpath(libs.google.maps.gradle.plugin)
    }
}