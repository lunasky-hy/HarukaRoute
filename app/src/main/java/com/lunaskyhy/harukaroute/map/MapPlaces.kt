package com.lunaskyhy.harukaroute.map

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.lunaskyhy.harukaroute.BuildConfig

object MapPlaces {
    private lateinit var placesClient: PlacesClient

    fun getPlacesClient(): PlacesClient {
        return placesClient
    }

    fun init(context: Context) {
        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty()) {
            Log.e("Places test", "No api key")
            return
        }

        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(context, apiKey)

        // Create a new PlacesClient instance
        placesClient = Places.createClient(context)
    }
}