package com.lunaskyhy.harukaroute.data

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.net.PlacesClient
import com.lunaskyhy.harukaroute.data.repos.DeviceLocationRepository
import com.lunaskyhy.harukaroute.data.repos.LocationRepository
import com.lunaskyhy.harukaroute.map.MapPlaces
import com.lunaskyhy.harukaroute.map.PlacesClientProvider

interface AppContainer {
    val locationRepository: LocationRepository
    val placesClient: PlacesClientProvider
    fun getLocationProviderClient(context: Context): FusedLocationProviderClient
}

class MyAppContainer(context: Context): AppContainer {
    override val locationRepository: LocationRepository by lazy {
        DeviceLocationRepository(LocationServices.getFusedLocationProviderClient(context))
    }

    override val placesClient: PlacesClientProvider by lazy {
        MapPlaces(context)
    }

    override fun getLocationProviderClient(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}