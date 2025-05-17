package com.lunaskyhy.harukaroute

import android.content.Context
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.trip.session.LocationObserver

open class HarukaMapNavigationObserver: MapboxNavigationObserver {
    private val locationObserver: LocationObserver = MyLocationObserver()

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        mapboxNavigation.registerLocationObserver(locationObserver)
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }
}

class HarukaMapNavigationObserverWithContext(
    private val context: Context,
) : HarukaMapNavigationObserver()