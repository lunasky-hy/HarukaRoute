package com.lunaskyhy.harukaroute.data

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.lunaskyhy.harukaroute.data.repos.DeviceLocationRepository
import com.lunaskyhy.harukaroute.data.repos.LocationRepository

interface AppContainer {
    val locationRepository: LocationRepository
    fun getLocationProviderClient(context: Context): FusedLocationProviderClient
}

class MyAppContainer(context: Context): AppContainer {
    override val locationRepository: LocationRepository by lazy {
        DeviceLocationRepository(LocationServices.getFusedLocationProviderClient(context))
    }

    override fun getLocationProviderClient(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}