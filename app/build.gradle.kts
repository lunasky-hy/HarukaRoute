plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.lunaskyhy.harukaroute"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lunaskyhy.harukaroute"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.app)
    implementation(libs.androidx.gms.location)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.mapbox.navigationcore)
    implementation(libs.mapbox.navigationcore.navigation)
    implementation(libs.mapbox.navigationcore.copilot)
    implementation(libs.mapbox.navigationcore.ui.maps)
    implementation(libs.mapbox.navigationcore.tripdata)
    implementation(libs.mapbox.navigationcore.voice)
    implementation(libs.mapbox.navigationcore.ui.components)
    implementation(libs.mapbox.navigationcore.androidauto)
    implementation(libs.mapbox.navigationcore.androidauto)
    implementation(libs.mapbox.extension.androidauto)

    implementation(libs.mapbox.search.autofill)
    implementation(libs.mapbox.search.discover)
    implementation(libs.mapbox.search.place.autocomplete)
    implementation(libs.mapbox.search.offline)
    implementation(libs.mapbox.search.mapbox.search.android)
    implementation(libs.mapbox.search.mapbox.search.android.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}