package com.lunaskyhy.harukaroute.data

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.lunaskyhy.harukaroute.BuildConfig

interface PlacesClientProvider {
    fun getPlacesClient(): PlacesClient
}

class MapPlacesApi(context: Context): PlacesClientProvider {
    private lateinit var placesClient: PlacesClient

    init {
        setup(context)
        Log.d("MapPlacesApi", "Initialized")
    }

    override fun getPlacesClient(): PlacesClient {
        return placesClient
    }

    private fun setup(context: Context) {
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