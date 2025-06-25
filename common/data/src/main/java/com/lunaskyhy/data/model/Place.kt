package com.lunaskyhy.data.model

import android.content.Intent
import androidx.core.net.toUri

data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

fun Place.toIntent(action: String): Intent {
    return Intent(action).apply {
        data = "geo:$latitude,$longitude".toUri()
    }
}