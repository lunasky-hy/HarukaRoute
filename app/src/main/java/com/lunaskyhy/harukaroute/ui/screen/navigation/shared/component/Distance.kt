package com.lunaskyhy.harukaroute.ui.screen.navigation.shared.component

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@SuppressLint("DefaultLocale")
@Composable
fun DisplayDistance(modifier: Modifier = Modifier, distance: Double,) {
    val distanceText = if (distance < 1000) String.format("%.0f m", distance)
        else String.format("%.1f km", distance / 1000)

    Text(text = distanceText, modifier = modifier)
}